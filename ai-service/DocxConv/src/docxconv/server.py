import shutil
import tempfile
import zipfile
from pathlib import Path

from fastapi import BackgroundTasks, FastAPI, File, HTTPException, Query, UploadFile
from fastapi.responses import FileResponse, JSONResponse

from docxconv.converters.extract_images import extract_images_metadata, extract_images_to_zip
from docxconv.converters.img import convert, docx_to_pdf, find_soffice
from docxconv.converters.json import convert_obj
from docxconv.converters.pandoc_img import convert as pandoc_convert

app = FastAPI(
    title="DocxConv",
    description="Convert DOCX/PDF to images, hierarchical JSON, or extract embedded images",
    version="0.4.0",
)


def _cleanup(path: Path | str):
    shutil.rmtree(path, ignore_errors=True)


# ---------------------------------------------------------------------------
# Image conversion
# ---------------------------------------------------------------------------

@app.post("/convert/img")
async def convert_to_images(
    file: UploadFile = File(...),
    dpi: int = Query(200, ge=72, le=600),
    format: str = Query("zip", pattern="^(zip|json)$"),
    pipeline: str = Query("libreoffice", pattern="^(libreoffice|pandoc)$"),
    annotate: str | None = Query(None, pattern="^(text|blocks)$"),
    bg: BackgroundTasks = None,  # type: ignore
):
    if not file.filename or not file.filename.lower().endswith(".docx"):
        raise HTTPException(400, "Only .docx files are accepted")

    tmpdir = Path(tempfile.mkdtemp())
    try:
        docx_path = tmpdir / file.filename
        with open(docx_path, "wb") as f:
            shutil.copyfileobj(file.file, f)

        if pipeline == "pandoc":
            img_paths = pandoc_convert(docx_path, tmpdir, dpi=dpi, annotate=annotate)
        else:
            img_paths = convert(docx_path, tmpdir, dpi=dpi)

        if format == "json":
            bg and bg.add_task(_cleanup, tmpdir)
            return {
                "pages": len(img_paths),
                "dpi": dpi,
                "pipeline": pipeline,
                "images": [p.name for p in img_paths],
            }

        stem = Path(file.filename).stem
        zip_path = tmpdir / f"{stem}.zip"
        with zipfile.ZipFile(zip_path, "w") as zf:
            for p in img_paths:
                zf.write(p, p.name)

        bg and bg.add_task(_cleanup, tmpdir)
        return FileResponse(
            zip_path,
            media_type="application/zip",
            filename=f"{stem}.zip",
        )
    except Exception:
        shutil.rmtree(tmpdir, ignore_errors=True)
        raise


@app.post("/convert/pdf")
async def convert_to_pdf(
    file: UploadFile = File(...),
    bg: BackgroundTasks = None,  # type: ignore
):
    """DOCX -> PDF only."""
    if not file.filename or not file.filename.lower().endswith(".docx"):
        raise HTTPException(400, "Only .docx files are accepted")

    tmpdir = Path(tempfile.mkdtemp())
    try:
        docx_path = tmpdir / file.filename
        with open(docx_path, "wb") as f:
            shutil.copyfileobj(file.file, f)

        pdf_path = docx_to_pdf(docx_path, tmpdir)
        stem = Path(file.filename).stem
        bg and bg.add_task(_cleanup, tmpdir)
        return FileResponse(pdf_path, media_type="application/pdf", filename=f"{stem}.pdf")
    except Exception:
        shutil.rmtree(tmpdir, ignore_errors=True)
        raise


# ---------------------------------------------------------------------------
# JSON conversion
# ---------------------------------------------------------------------------

@app.post("/convert/json")
async def convert_to_json(file: UploadFile = File(...)):
    if not file.filename or not file.filename.lower().endswith(".docx"):
        raise HTTPException(status_code=400, detail="Only .docx files are accepted")

    try:
        content = await file.read()
    except Exception:
        raise HTTPException(status_code=400, detail="Failed to read uploaded file")

    with tempfile.NamedTemporaryFile(suffix=".docx", delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)

    try:
        result = convert_obj(tmp_path)
        return JSONResponse(content=result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Conversion failed: {e}")
    finally:
        tmp_path.unlink(missing_ok=True)


# ---------------------------------------------------------------------------
# Image extraction (DOCX/PDF → embedded images)
# ---------------------------------------------------------------------------

@app.post("/extract/images")
async def extract_images(
    file: UploadFile = File(...),
    format: str = Query("zip", pattern="^(zip|json)$"),
    bg: BackgroundTasks = None,  # type: ignore
):
    if not file.filename:
        raise HTTPException(400, "No filename provided")
    s = Path(file.filename).suffix.lower()
    if s not in (".docx", ".pdf"):
        raise HTTPException(400, "Only .docx and .pdf files are accepted")

    tmpdir = Path(tempfile.mkdtemp())
    try:
        doc_path = tmpdir / file.filename
        with open(doc_path, "wb") as f:
            shutil.copyfileobj(file.file, f)

        if format == "json":
            bg and bg.add_task(_cleanup, tmpdir)
            return extract_images_metadata(doc_path)

        stem = Path(file.filename).stem
        zip_path = extract_images_to_zip(doc_path, tmpdir)
        bg and bg.add_task(_cleanup, tmpdir)
        return FileResponse(
            zip_path,
            media_type="application/zip",
            filename=f"{stem}_images.zip",
        )
    except Exception:
        shutil.rmtree(tmpdir, ignore_errors=True)
        raise


# ---------------------------------------------------------------------------
# Health
# ---------------------------------------------------------------------------

@app.get("/health")
async def health():
    soffice = find_soffice()
    return {
        "status": "ok" if Path(soffice).exists() else "degraded",
        "soffice": soffice,
    }


def main():
    import uvicorn
    uvicorn.run("docxconv.server:app", host="0.0.0.0", port=8000, reload=False)


if __name__ == "__main__":
    main()
