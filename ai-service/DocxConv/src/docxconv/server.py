import tempfile
from pathlib import Path

from fastapi import FastAPI, File, HTTPException, UploadFile

import config
from docxconv.converters.img import find_soffice
from docxconv.converters.json import convert_obj
from evaluator.deepseek import evaluate as evaluate_content

app = FastAPI(
    title="AI Evaluation Service",
    description="File preprocessing and AI evaluation",
    version="0.7.0",
)

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
    """Write content to a temp file and call fn(path)."""
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)
    try:
        return fn(tmp_path)
    finally:
        tmp_path.unlink(missing_ok=True)


# docx ──────────────────────────────────────────────────────────────────

def _extract_docx_text(content: bytes) -> tuple[str, list]:
    def _do(tmp_path):
        structured = convert_obj(tmp_path)
        lines = []
        for item in structured:
            _flatten_content(item, lines)
        return "\n\n".join(lines), structured
    return _with_temp(content, ".docx", _do)


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


# pdf ───────────────────────────────────────────────────────────────────

def _extract_pdf_text(content: bytes) -> str:
    import fitz
    def _do(tmp_path):
        doc = fitz.open(str(tmp_path))
        pages = [page.get_text() for page in doc]
        doc.close()
        return "\n\n".join(p for p in pages if p.strip())
    return _with_temp(content, ".pdf", _do)


# xlsx ──────────────────────────────────────────────────────────────────

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


# pptx ──────────────────────────────────────────────────────────────────

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


# image (OCR) ───────────────────────────────────────────────────────────

def _extract_image_text(content: bytes) -> str:
    from PIL import Image
    import io
    from screenshotproc.ocr import ocr_image

    img = Image.open(io.BytesIO(content))
    results = ocr_image(img)
    if not results:
        return ""
    # sort by reading order and join lines
    results.sort(key=lambda r: (round(r["bbox"][1] / 30) * 30, r["bbox"][0]))
    return "\n".join(r["text"] for r in results)


# archive ───────────────────────────────────────────────────────────────

def _extract_archive_text(content: bytes, filename: str) -> tuple[str, list[str]]:
    import shutil
    from archiveproc.extractors import process

    tmpdir = Path(tempfile.mkdtemp())
    try:
        arc_path = tmpdir / filename
        arc_path.write_bytes(content)
        result = process(arc_path)
        # collect text from extracted files
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


# ── wrapper: all file types → extractedText ───────────────────────────

def _extract_text(content: bytes, filename: str) -> tuple[str, list]:
    """Return (extractedText, warnings) for any file type."""
    suffix = Path(filename).suffix.lower()

    # text files
    if suffix in config.TEXT_EXTENSIONS:
        return _extract_text_file(content), []

    # docx
    if suffix == ".docx":
        text, _ = _extract_docx_text(content)
        return text, []

    # doc (old format, limited support)
    if suffix == ".doc":
        try:
            text, _ = _extract_docx_text(content)
            return text, ["旧版 .doc 格式，部分内容可能丢失"]
        except Exception:
            return "", [".doc 格式不受支持，请转为 .docx 后重试"]

    # pdf
    if suffix == ".pdf":
        text = _extract_pdf_text(content)
        if not text.strip():
            return "", ["PDF 中未提取到文本（可能是扫描件，当前版本不支持扫描件 OCR）"]
        return text, []

    # xlsx / xls
    if suffix in (".xlsx", ".xls"):
        try:
            return _extract_xlsx_text(content), []
        except Exception as e:
            return "", [f"表格解析失败: {e}"]

    # pptx / ppt
    if suffix in (".pptx", ".ppt"):
        try:
            return _extract_pptx_text(content), []
        except Exception as e:
            return "", [f"演示文稿解析失败: {e}"]

    # images (OCR)
    if suffix in (".png", ".jpg", ".jpeg", ".bmp", ".webp"):
        try:
            text = _extract_image_text(content)
            if not text.strip():
                return "", ["图片中未识别到文字"]
            return text, ["图片内容由 OCR 识别，可能存在错字"]
        except Exception as e:
            return "", [f"OCR 识别失败: {e}"]

    # archives
    if suffix in (".zip", ".tar", ".tar.xz", ".rar", ".7z"):
        text, warnings = _extract_archive_text(content, filename)
        return text, warnings

    # binary / unknown → try text as last resort
    try:
        return content.decode("utf-8"), ["无法识别文件类型，已尝试当作文本读取，结果可能不正确"]
    except UnicodeDecodeError:
        return "", [f"不支持的文件格式（{suffix}），无法提取文本"]


# ── /api/preprocess ───────────────────────────────────────────────────

@app.post("/api/preprocess")
async def preprocess(file: UploadFile = File(...)):
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
    text, warnings = _extract_text(content, file.filename)

    result = {
        "fileType": ft,
        "originalFilename": file.filename,
        "extractedText": text,
        "warnings": warnings,
    }

    # docx: also include structured JSON for future use
    if Path(file.filename).suffix.lower() == ".docx":
        try:
            _, structured = _extract_docx_text(content)
            result["structuredContent"] = structured
        except Exception:
            pass

    return result


# ── /api/evaluate (fake, backward compat) ──────────────────────────────

@app.post("/api/evaluate")
async def evaluate(studentName: str = "", fileName: str = ""):
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

@app.post("/api/evaluate/real")
async def evaluate_real(file: UploadFile = File(...), studentName: str = ""):
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


# ── /api/health ───────────────────────────────────────────────────────

@app.get("/api/health")
async def health():
    libre_ok = Path(find_soffice()).exists()
    return {
        "status": "degraded" if libre_ok else "unavailable",
        "libreofficeAvailable": libre_ok,
    }


def main():
    import uvicorn
    uvicorn.run("docxconv.server:app", host="0.0.0.0", port=8000, reload=False)


if __name__ == "__main__":
    main()
