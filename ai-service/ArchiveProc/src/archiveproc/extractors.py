from __future__ import annotations

import shutil
import tarfile
import tempfile
import zipfile
from pathlib import Path
from typing import Optional

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

MAX_TEXT_SIZE = 5 * 1024 * 1024  # 5 MB
INLINE_BYTES_MAX = 50 * 1024 * 1024  # 50 MB — raw bytes kept inline in result

TEXT_EXTENSIONS = {
    ".txt", ".java", ".py", ".js", ".ts", ".jsx", ".tsx", ".html", ".htm",
    ".css", ".scss", ".less", ".json", ".xml", ".yaml", ".yml", ".md",
    ".sql", ".c", ".cpp", ".cc", ".cxx", ".h", ".hpp", ".cs", ".go", ".rs",
    ".kt", ".kts", ".swift", ".rb", ".php", ".sh", ".bash", ".zsh", ".bat",
    ".cmd", ".ps1", ".properties", ".gradle", ".cfg", ".ini", ".toml",
    ".env", ".gitignore", ".dockerignore", ".editorconfig", ".rst",
    ".tex", ".log", ".csv", ".tsv", ".proto", ".graphql", ".vue", ".svelte",
    ".r", ".m", ".scala", ".clj", ".cljs", ".edn", ".erl", ".hrl",
    ".ex", ".exs", ".hs", ".lhs", ".ml", ".mli", ".dart", ".lua", ".pl",
    ".pm", ".tcl", ".vim", ".el", ".jl",
}

DOCX_EXTENSIONS = {".docx"}
OCR_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp", ".bmp", ".gif", ".tiff"}

# Extensions that indicate a (potentially nested) archive
ARCHIVE_EXTENSIONS = {
    ".zip", ".rar", ".7z",
    ".tar", ".tar.gz", ".tgz", ".tar.bz2", ".tbz2", ".tar.xz", ".txz",
    ".gz", ".bz2", ".xz",
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _suffixes(path: Path) -> set[str]:
    name = path.name.lower()
    s = {path.suffix.lower()} if path.suffix else set()
    if name.endswith(".tar.gz"):
        s.add(".tar.gz")
    elif name.endswith(".tar.bz2"):
        s.add(".tar.bz2")
    elif name.endswith(".tar.xz"):
        s.add(".tar.xz")
    return s


def _is_archive(path: Path) -> bool:
    return bool(_suffixes(path) & ARCHIVE_EXTENSIONS)


def classify(path: Path) -> str:
    """Classify a single extracted file by extension."""
    s = _suffixes(path)
    if s & TEXT_EXTENSIONS:
        return "text"
    if s & DOCX_EXTENSIONS:
        return "needs_docxconv"
    if s & OCR_EXTENSIONS:
        return "needs_ocr"
    return "unsupported"


def _read_text(path: Path) -> Optional[str]:
    """Read file as UTF-8 text. Returns None if too large or not valid UTF-8."""
    if path.stat().st_size > MAX_TEXT_SIZE:
        return None
    try:
        return path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        return None


# ---------------------------------------------------------------------------
# Single-archive extraction
# ---------------------------------------------------------------------------

def _extract_one(path: Path, dest: Path) -> None:
    """Extract a single archive to dest. Raises on unsupported format."""
    name = path.name.lower()
    s = _suffixes(path)

    if ".zip" in s:
        with zipfile.ZipFile(path, "r") as zf:
            zf.extractall(dest)

    elif ".7z" in s:
        import py7zr
        with py7zr.SevenZipFile(path, "r") as zf:
            zf.extractall(dest)

    elif ".rar" in s:
        import rarfile
        with rarfile.RarFile(path, "r") as rf:
            rf.extractall(dest)

    elif ".tar" in s or any(x in s for x in {".tar.gz", ".tgz", ".tar.bz2", ".tbz2", ".tar.xz", ".txz"}):
        with tarfile.open(path, "r:*") as tf:
            tf.extractall(dest, filter="data")

    elif ".gz" in s and ".tar" not in name:
        import gzip
        out_name = path.stem
        with gzip.open(path, "rb") as f_in:
            with open(dest / out_name, "wb") as f_out:
                shutil.copyfileobj(f_in, f_out)

    elif ".bz2" in s and ".tar" not in name:
        import bz2
        out_name = path.stem
        with bz2.open(path, "rb") as f_in:
            with open(dest / out_name, "wb") as f_out:
                shutil.copyfileobj(f_in, f_out)

    elif ".xz" in s and ".tar" not in name:
        import lzma
        out_name = path.stem
        with lzma.open(path, "rb") as f_in:
            with open(dest / out_name, "wb") as f_out:
                shutil.copyfileobj(f_in, f_out)

    else:
        raise ValueError(f"Unsupported archive format: {path.name}")


# ---------------------------------------------------------------------------
# Recursive extraction + classification
# ---------------------------------------------------------------------------

def process(path: Path) -> dict:
    """Fully extract an archive, recursively expanding nested archives.

    Returns the standard file-tree dict (see README). The archive is extracted
    into a temporary directory that is cleaned up after processing.
    """
    tmpdir = Path(tempfile.mkdtemp())

    try:
        _extract_one(path, tmpdir)

        # Recursively expand nested archives
        changed = True
        while changed:
            changed = False
            for p in sorted(tmpdir.rglob("*")):
                if not p.is_file():
                    continue
                if _is_archive(p):
                    subdir = p.parent / f"_extracted_{p.stem}"
                    subdir.mkdir(exist_ok=True)
                    try:
                        _extract_one(p, subdir)
                        p.unlink()  # remove the archive after successful extraction
                        changed = True
                    except Exception:
                        # If nested extraction fails, leave the file as-is
                        pass

        # Collect all extracted files (excluding dirs)
        all_files = sorted(p for p in tmpdir.rglob("*") if p.is_file())

        # Build result
        files: list[dict] = []
        counts: dict[str, int] = {"text": 0, "needs_docxconv": 0, "needs_ocr": 0, "unsupported": 0}

        for fp in all_files:
            rel = fp.relative_to(tmpdir).as_posix()
            ftype = classify(fp)
            size = fp.stat().st_size
            entry: dict = {"path": rel, "type": ftype, "size": size}

            if ftype == "text":
                text = _read_text(fp)
                if text is not None:
                    entry["text"] = text
                else:
                    entry["text_truncated"] = True
            elif size <= INLINE_BYTES_MAX:
                try:
                    entry["bytes"] = fp.read_bytes()
                except OSError:
                    pass

            files.append(entry)
            counts[ftype] += 1

        return {
            "files": files,
            "summary": {
                "total": len(files),
                **counts,
            },
        }

    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)
