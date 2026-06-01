from __future__ import annotations

import shutil
import tempfile
from pathlib import Path

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from archiveproc.extractors import process

ALLOWED_TYPES = {
    "application/zip",
    "application/x-zip-compressed",
    "application/x-rar-compressed",
    "application/x-7z-compressed",
    "application/x-tar",
    "application/gzip",
    "application/x-bzip2",
    "application/x-xz",
    "application/octet-stream",  # some browsers send archives as this
}
MAX_SIZE_MB = 200

app = FastAPI(
    title="ArchiveProc",
    description="Extract archives (ZIP/RAR/7z/TAR) and return a classified file tree",
    version="0.1.0",
)


def _check_file(file: UploadFile) -> None:
    if file.size and file.size > MAX_SIZE_MB * 1024 * 1024:
        raise HTTPException(413, f"File too large (max {MAX_SIZE_MB} MB)")


@app.post("/extract")
async def extract_endpoint(file: UploadFile = File(...)):
    _check_file(file)

    tmpdir = Path(tempfile.mkdtemp())
    try:
        name = file.filename or "upload.zip"
        dest = tmpdir / name
        with open(dest, "wb") as out:
            shutil.copyfileobj(file.file, out)

        result = process(dest)
        return JSONResponse(content=result)
    except ValueError as e:
        raise HTTPException(400, str(e))
    except Exception as e:
        raise HTTPException(500, f"Extraction failed: {e}")
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)


@app.get("/health")
async def health():
    features = {"zip": True, "tar": True}

    try:
        import py7zr  # noqa: F401
        features["7z"] = True
    except ImportError:
        features["7z"] = False

    try:
        import rarfile  # noqa: F401
        if rarfile.tool_setup():
            features["rar"] = True
        else:
            features["rar"] = False
    except Exception:
        features["rar"] = False

    all_ok = all(features.values())
    return {"status": "ok" if all_ok else "degraded", "features": features}


def main():
    import uvicorn
    uvicorn.run("archiveproc.server:app", host="0.0.0.0", port=8000, reload=False)


if __name__ == "__main__":
    main()
