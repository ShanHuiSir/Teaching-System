import tempfile
from pathlib import Path

from fastapi import FastAPI, File, HTTPException, UploadFile

from docxconv.converters.img import find_soffice
from docxconv.converters.json import convert_obj
from docxconv.evaluator import evaluate as evaluate_content

app = FastAPI(
    title="AI Evaluation Service",
    description="File preprocessing and AI evaluation",
    version="0.6.0",
)

# ── file type dispatch ────────────────────────────────────────────────

_TEXT_EXTENSIONS = {
    ".txt", ".md", ".csv", ".log",
    ".cpp", ".c", ".h", ".hpp", ".java", ".py", ".js", ".ts",
    ".html", ".css", ".scss", ".xml", ".json", ".yaml", ".yml",
    ".sql", ".sh", ".bat", ".ps1", ".ini", ".cfg", ".toml",
}


def _is_text_file(filename: str) -> bool:
    return Path(filename).suffix.lower() in _TEXT_EXTENSIONS


# ── extractors ────────────────────────────────────────────────────────

def _extract_text_file(content: bytes) -> str:
    for encoding in ("utf-8", "gbk", "utf-16"):
        try:
            return content.decode(encoding)
        except (UnicodeDecodeError, UnicodeError):
            continue
    return content.decode("utf-8", errors="replace")


def _extract_docx_text(content: bytes) -> tuple[str, list]:
    """Return (plain_text, structured_content)."""
    with tempfile.NamedTemporaryFile(suffix=".docx", delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)

    try:
        structured = convert_obj(tmp_path)
    except Exception as e:
        raise HTTPException(500, f"DOCX parsing failed: {e}")
    finally:
        tmp_path.unlink(missing_ok=True)

    lines = []
    for item in structured:
        _flatten_content(item, lines)
    return "\n\n".join(lines), structured


def _flatten_content(item: dict, lines: list):
    """Recursively flatten structured content to plain text lines."""
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
    """Extract text from PDF using PyMuPDF."""
    import fitz

    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)

    try:
        doc = fitz.open(str(tmp_path))
        pages = [page.get_text() for page in doc]
        doc.close()
    except Exception as e:
        raise HTTPException(500, f"PDF parsing failed: {e}")
    finally:
        tmp_path.unlink(missing_ok=True)

    return "\n\n".join(p for p in pages if p.strip())


# ── /api/preprocess ───────────────────────────────────────────────────

@app.post("/api/preprocess")
async def preprocess(file: UploadFile = File(...)):
    if not file.filename:
        raise HTTPException(400, "No filename provided")

    warnings = []
    suffix = Path(file.filename).suffix.lower()

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

    # plain text
    if _is_text_file(file.filename):
        text = _extract_text_file(content)
        return {
            "fileType": suffix.lstrip("."),
            "originalFilename": file.filename,
            "extractedText": text,
            "warnings": [],
        }

    # docx
    if suffix == ".docx":
        text, structured = _extract_docx_text(content)
        return {
            "fileType": "docx",
            "originalFilename": file.filename,
            "extractedText": text,
            "structuredContent": structured,
            "warnings": [],
        }

    # pdf
    if suffix == ".pdf":
        text = _extract_pdf_text(content)
        if not text.strip():
            return {
                "fileType": "pdf",
                "originalFilename": file.filename,
                "extractedText": "",
                "warnings": ["PDF 中未提取到文本（可能是扫描件），当前版本不支持扫描件 OCR"],
            }
        return {
            "fileType": "pdf",
            "originalFilename": file.filename,
            "extractedText": text,
            "warnings": [],
        }

    # unsupported
    return {
        "fileType": suffix.lstrip(".") or "unknown",
        "originalFilename": file.filename,
        "extractedText": "",
        "warnings": [f"暂不支持 {suffix} 文件格式，当前支持：文本文件、.docx、.pdf"],
    }


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


# ── /api/evaluate/real (file upload + preprocess → AI scoring placeholder)

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
