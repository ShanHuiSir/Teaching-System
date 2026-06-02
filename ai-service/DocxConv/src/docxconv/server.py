import tempfile
from pathlib import Path
from typing import Any, List, Optional

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

import config
from docxconv.converters.img import find_soffice
from docxconv.converters.json import convert_obj
from evaluator.deepseek import evaluate as evaluate_content

app = FastAPI(
    title="AI Evaluation Service",
    description="文件预处理与 AI 自动评分服务",
    version="0.7.0",
    servers=[
        {"url": config.SERVER_URL, "description": "当前环境"},
    ],
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=config.CORS_ORIGINS,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── response models ─────────────────────────────────────────────────────

class PreprocessResponse(BaseModel):
    fileType: str = Field(description="文件类型：text / docx / pdf / xlsx / pptx / 图片后缀 / zip 等 / empty / unknown")
    originalFilename: str = Field(description="上传时的原始文件名")
    extractedText: str = Field(description="提取的纯文本内容，供 AI 评分使用")
    warnings: list[str] = Field(default_factory=list, description="预处理过程中的警告信息")
    structuredContent: Optional[List[Any]] = Field(None, description="仅 .docx 返回：标题/段落/列表/表格的结构化 JSON")

class EvaluateResponse(BaseModel):
    aiScore: float = Field(description="AI 评分，0-100")
    aiIssues: str = Field(description="扣分项列表，每条以 N. 开头，\\n 分隔")
    aiComment: str = Field(description="50-150 字综合评价")
    status: int = Field(description="固定为 1，表示正常返回")

class EvaluateRealResponse(PreprocessResponse):
    studentName: str = Field(description="学生姓名")
    aiScore: float = Field(description="AI 评分，0-100")
    aiIssues: str = Field(description="扣分项列表，每条以 N. 开头，\\n 分隔")
    aiComment: str = Field(description="50-150 字综合评价")
    status: int = Field(description="固定为 1，表示正常返回")

class HealthResponse(BaseModel):
    status: str = Field(description="degraded = LibreOffice 可用 / unavailable = 不可用")
    libreofficeAvailable: bool = Field(description="LibreOffice 是否已安装并可用")

# ── supported file types ──────────────────────────────────────────────

def _file_type(filename: str) -> str:
    suffix = Path(filename).suffix.lower()
    if suffix in config.TEXT_EXTENSIONS:
        return "text"
    if suffix in config.TOOL_EXTENSIONS:
        return suffix.lstrip(".")
    return "unknown"

# ── extractors ────────────────────────────────────────────────────────

def _extract_text_file(content: bytes) -> str:
    for encoding in ("utf-8", "gbk", "utf-16"):
        try:
            return content.decode(encoding)
        except (UnicodeDecodeError, UnicodeError):
            continue
    return content.decode("utf-8", errors="replace")

def _with_temp(content: bytes, suffix: str, fn):
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)
    try:
        return fn(tmp_path)
    finally:
        tmp_path.unlink(missing_ok=True)

def _extract_docx_text(content: bytes) -> tuple[str, list]:
    def _do(tmp_path):
        structured = convert_obj(tmp_path)
        lines = []
        for item in structured:
            _flatten_content(item, lines)
        return "\n\n".join(lines), structured
    return _with_temp(content, ".docx", _do)

def _extract_docx_images_ocr(content: bytes) -> list[dict]:
    """Extract embedded images from a DOCX file and run OCR on each.

    Returns a list of {"context": str|None, "ocr_text": str} dicts.
    context is the surrounding paragraph text used for position matching.
    """
    import io as _io
    from PIL import Image as PILImage
    from docxconv.converters.extract_images import extract_images
    from screenshotproc.ocr import ocr_image

    def _do(tmp_path):
        images = extract_images(tmp_path)
        results = []
        for img_info in images:
            ocr_text = ""
            try:
                pil_img = PILImage.open(_io.BytesIO(img_info.raw_bytes))
                ocr_results = ocr_image(pil_img)
                if ocr_results:
                    ocr_results.sort(key=lambda r: (round(r["bbox"][1] / 30) * 30, r["bbox"][0]))
                    lines = [r["text"] for r in ocr_results if r["text"].strip()]
                    if lines:
                        ocr_text = "\n".join(lines)
            except Exception:
                pass
            if ocr_text:
                results.append({
                    "context": img_info.context,
                    "ocr_text": f"[图片 {img_info.index} ({img_info.width}x{img_info.height})]\n{ocr_text}",
                })
        return results

    return _with_temp(content, ".docx", _do)


def _insert_ocr_at_context(text: str, context: Optional[str], ocr_block: str) -> str:
    """Insert ocr_block into text near the matching context string.

    If context is given and found in text, inserts right after the last
    occurrence. Otherwise appends at the end.
    """
    if context:
        idx = text.rfind(context.strip())
        if idx != -1:
            insert_at = idx + len(context.strip())
            return text[:insert_at] + "\n\n" + ocr_block + "\n" + text[insert_at:]
    return text + "\n\n" + ocr_block


def _flatten_content(item: dict, lines: list):
    if "heading" in item:
        prefix = "#" * item.get("level", 1)
        lines.append(f"{prefix} {item['heading']}")
        for sub in item.get("content", []):
            _flatten_content(sub, lines)
        for child in item.get("children", []):
            _flatten_content(child, lines)
    elif item.get("type") == "paragraph":
        lines.append(item.get("text", ""))
    elif item.get("type") == "list":
        for li in item.get("items", []):
            prefix = "- " if item.get("ordered") else "* "
            lines.append(f"{prefix}{li.get('text', '')}")
            for child in li.get("children", []):
                lines.append(f"  - {child.get('text', '')}")
    elif item.get("type") == "table":
        for row in item.get("rows", []):
            lines.append(" | ".join(row))

def _extract_pdf_text(content: bytes) -> str:
    import fitz
    def _do(tmp_path):
        doc = fitz.open(str(tmp_path))
        pages = [page.get_text() for page in doc]
        doc.close()
        return "\n\n".join(p for p in pages if p.strip())
    return _with_temp(content, ".pdf", _do)

def _extract_xlsx_text(content: bytes) -> str:
    import openpyxl
    def _do(tmp_path):
        wb = openpyxl.load_workbook(tmp_path, data_only=True, read_only=True)
        parts = []
        for name in wb.sheetnames:
            ws = wb[name]
            rows = []
            for row in ws.iter_rows(values_only=True):
                cells = [str(c) for c in row if c is not None]
                if cells:
                    rows.append(" | ".join(cells))
            if rows:
                parts.append(f"--- 工作表: {name} ---\n" + "\n".join(rows))
        wb.close()
        return "\n\n".join(parts)
    return _with_temp(content, ".xlsx", _do)

def _extract_pptx_text(content: bytes) -> str:
    from pptx import Presentation
    def _do(tmp_path):
        prs = Presentation(str(tmp_path))
        parts = []
        for i, slide in enumerate(prs.slides, 1):
            texts = []
            for shape in slide.shapes:
                if shape.has_text_frame:
                    for para in shape.text_frame.paragraphs:
                        t = para.text.strip()
                        if t:
                            texts.append(t)
                if shape.has_table:
                    for row in shape.table.rows:
                        cells = [cell.text.strip() for cell in row.cells]
                        texts.append(" | ".join(c for c in cells if c))
            if texts:
                parts.append(f"--- 幻灯片 {i} ---\n" + "\n".join(texts))
        return "\n\n".join(parts)
    return _with_temp(content, ".pptx", _do)

def _extract_image_text(content: bytes) -> str:
    from PIL import Image
    import io
    from screenshotproc.ocr import ocr_image

    img = Image.open(io.BytesIO(content))
    results = ocr_image(img)
    if not results:
        return ""
    results.sort(key=lambda r: (round(r["bbox"][1] / 30) * 30, r["bbox"][0]))
    return "\n".join(r["text"] for r in results)

def _extract_archive_text(content: bytes, filename: str) -> tuple[str, list[str]]:
    import shutil
    from archiveproc.extractors import process

    tmpdir = Path(tempfile.mkdtemp())
    try:
        arc_path = tmpdir / filename
        arc_path.write_bytes(content)
        result = process(arc_path)
        texts = []
        warnings = result.get("warnings", [])
        for extracted in result.get("files", []):
            epath = Path(extracted.get("path", ""))
            if epath.suffix.lower() in config.TEXT_EXTENSIONS:
                try:
                    texts.append(f"--- {epath.name} ---\n{epath.read_text(encoding='utf-8')}")
                except Exception:
                    pass
        return "\n\n".join(texts), warnings
    except Exception as e:
        return "", [f"解压失败: {e}"]
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)

def _extract_text(content: bytes, filename: str) -> tuple[str, list]:
    suffix = Path(filename).suffix.lower()
    if suffix in config.TEXT_EXTENSIONS:
        return _extract_text_file(content), []
    if suffix == ".docx":
        text, _ = _extract_docx_text(content)
        return text, []
    if suffix == ".doc":
        try:
            text, _ = _extract_docx_text(content)
            return text, ["旧版 .doc 格式，部分内容可能丢失"]
        except Exception:
            return "", [".doc 格式不受支持，请转为 .docx 后重试"]
    if suffix == ".pdf":
        text = _extract_pdf_text(content)
        if not text.strip():
            return "", ["PDF 中未提取到文本（可能是扫描件，当前版本不支持扫描件 OCR）"]
        return text, []
    if suffix in (".xlsx", ".xls"):
        try:
            return _extract_xlsx_text(content), []
        except Exception as e:
            return "", [f"表格解析失败: {e}"]
    if suffix in (".pptx", ".ppt"):
        try:
            return _extract_pptx_text(content), []
        except Exception as e:
            return "", [f"演示文稿解析失败: {e}"]
    if suffix in (".png", ".jpg", ".jpeg", ".bmp", ".webp"):
        try:
            text = _extract_image_text(content)
            if not text.strip():
                return "", ["图片中未识别到文字"]
            return text, ["图片内容由 OCR 识别，可能存在错字"]
        except Exception as e:
            return "", [f"OCR 识别失败: {e}"]
    if suffix in (".zip", ".tar", ".tar.xz", ".rar", ".7z"):
        text, warnings = _extract_archive_text(content, filename)
        return text, warnings
    try:
        return content.decode("utf-8"), ["无法识别文件类型，已尝试当作文本读取，结果可能不正确"]
    except UnicodeDecodeError:
        return "", [f"不支持的文件格式（{suffix}），无法提取文本"]

# ── /api/health ───────────────────────────────────────────────────────

@app.get(
    "/api/health",
    response_model=HealthResponse,
    summary="健康检查",
    description="返回服务运行状态及 LibreOffice 是否可用。",
    tags=["系统"],
)
async def health():
    libre_ok = Path(find_soffice()).exists()
    return {
        "status": "degraded" if libre_ok else "unavailable",
        "libreofficeAvailable": libre_ok,
    }

# ── /api/preprocess ───────────────────────────────────────────────────

@app.post(
    "/api/preprocess",
    response_model=PreprocessResponse,
    summary="文件预处理",
    description="上传任意格式文件，提取纯文本内容。支持 .txt/.docx/.pdf/.xlsx/.pptx/图片(OCR)/压缩包等。.docx 额外返回结构化 JSON。",
    tags=["预处理"],
)
async def preprocess(file: UploadFile = File(..., description="要预处理的文件")):
    if not file.filename:
        raise HTTPException(400, "No filename provided")
    try:
        content = await file.read()
    except Exception:
        raise HTTPException(400, "Failed to read uploaded file")
    if not content:
        return {
            "fileType": "empty",
            "originalFilename": file.filename,
            "extractedText": "",
            "warnings": ["文件内容为空"],
        }
    ft = _file_type(file.filename)
    is_docx = Path(file.filename).suffix.lower() == ".docx"

    if is_docx:
        text, structured = _extract_docx_text(content)
        warnings = []
    else:
        text, warnings = _extract_text(content, file.filename)
        structured = None

    result = {
        "fileType": ft,
        "originalFilename": file.filename,
        "extractedText": text,
        "warnings": warnings,
    }
    if structured is not None:
        result["structuredContent"] = structured

    if is_docx:
        try:
            ocr_results = _extract_docx_images_ocr(content)
            if ocr_results:
                text = result["extractedText"]
                for img in ocr_results:
                    text = _insert_ocr_at_context(text, img["context"], img["ocr_text"])
                result["extractedText"] = text
                warnings.append("文档中的图片内容由 OCR 识别，可能存在错字")
        except Exception:
            pass
    return result

# ── /api/evaluate (fake, backward compat) ──────────────────────────────

@app.post(
    "/api/evaluate",
    response_model=EvaluateResponse,
    summary="AI 评分（假）",
    description="不依赖 Python 预处理，直接返回硬编码评分。用于离线演示和开发调试。",
    tags=["评分"],
)
async def evaluate(
    studentName: str = "",
    fileName: str = "",
):
    if not studentName.strip():
        raise HTTPException(400, "studentName is required")
    return {
        "aiScore": 82.50,
        "aiIssues": (
            "1. 结构不够清晰，建议优化段落层次\n"
            "2. 缺少核心论点支撑材料\n"
            "3. 格式规范性不足，标题层级需统一"
        ),
        "aiComment": "整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。",
        "status": 1,
    }

# ── /api/evaluate/real (file upload → preprocess → AI) ────────────────

@app.post(
    "/api/evaluate/real",
    response_model=EvaluateRealResponse,
    summary="AI 评分（真实）",
    description="上传文件 → 预处理提取文本 → DeepSeek 评分，返回分数、扣分项和评语。",
    tags=["评分"],
)
async def evaluate_real(
    file: UploadFile = File(..., description="学生提交的作业文件"),
    studentName: str = Form(""),
):
    if not studentName.strip():
        raise HTTPException(400, "studentName is required")
    preprocess_result = await preprocess(file)
    try:
        eval_result = evaluate_content(preprocess_result["extractedText"], studentName)
    except RuntimeError as e:
        raise HTTPException(503, f"AI evaluation failed: {e}")
    return {
        "studentName": studentName,
        "originalFilename": preprocess_result["originalFilename"],
        "fileType": preprocess_result["fileType"],
        "extractedText": preprocess_result["extractedText"],
        "warnings": preprocess_result["warnings"],
        "aiScore": eval_result["aiScore"],
        "aiIssues": eval_result["aiIssues"],
        "aiComment": eval_result["aiComment"],
        "status": 1,
    }

def main():
    import uvicorn
    uvicorn.run("docxconv.server:app", host="0.0.0.0", port=8000, reload=False)

if __name__ == "__main__":
    main()
