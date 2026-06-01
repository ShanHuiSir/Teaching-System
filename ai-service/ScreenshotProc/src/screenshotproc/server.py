from __future__ import annotations

import shutil
import tempfile
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from screenshotproc import ocr_images

ALLOWED_TYPES = {"image/png", "image/jpeg", "image/webp"}
MAX_SIZE_MB = 50


def _check_file(file: UploadFile) -> None:
    if file.content_type and file.content_type not in ALLOWED_TYPES:
        raise HTTPException(
            400,
            f"Unsupported type '{file.content_type}'. "
            f"Allowed: {', '.join(sorted(ALLOWED_TYPES))}",
        )
    if file.size and file.size > MAX_SIZE_MB * 1024 * 1024:
        raise HTTPException(413, f"File too large (max {MAX_SIZE_MB} MB)")


@asynccontextmanager
async def lifespan(app: FastAPI):
    from screenshotproc.ocr import _load_models

    _load_models()
    yield


app = FastAPI(
    title="ScreenshotProc",
    description="Extract text from screenshots via Surya OCR — line-level JSON with bounding boxes",
    version="0.1.0",
    lifespan=lifespan,
)


@app.post("/ocr")
async def ocr_endpoint(files: list[UploadFile] = File(..., min_length=1)):
    for f in files:
        _check_file(f)

    tmpdir = Path(tempfile.mkdtemp())
    try:
        paths: list[Path] = []
        names: list[str] = []
        for f in files:
            name = f.filename or "upload.png"
            dest = tmpdir / name
            with open(dest, "wb") as out:
                shutil.copyfileobj(f.file, out)
            paths.append(dest)
            names.append(name)

        result = ocr_images(paths, filenames=names)
        return JSONResponse(content=result)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"OCR failed: {e}")
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)


@app.get("/health")
async def health():
    from screenshotproc.ocr import _det_predictor, _rec_predictor

    models_loaded = _det_predictor is not None and _rec_predictor is not None
    return {
        "status": "ok" if models_loaded else "degraded",
        "models_loaded": models_loaded,
    }


def main():
    import uvicorn

    uvicorn.run("screenshotproc.server:app", host="0.0.0.0", port=8000, reload=False)


if __name__ == "__main__":
    main()
