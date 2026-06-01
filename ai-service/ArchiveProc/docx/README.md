# ArchiveProc

Extract archives (ZIP / RAR / 7z / TAR) and return a classified file tree
with inline text content. Nested archives are recursively expanded. Does NOT
call other preprocessing tools — it only categorizes files so the orchestrator
knows what to do next.

## Installation

```bash
uv venv --python 3.13
uv pip install -e .
```

Requires Python >= 3.13.

### Format support

| Format | Dependency | Status |
|---|---|---|
| ZIP | built-in | Always available |
| TAR / tar.gz / tar.bz2 / tar.xz | built-in | Always available |
| 7z | `py7zr` (pure Python) | Always available |
| RAR | `rarfile` + system `unrar` | Requires system tool |

Check `/health` for per-format availability at runtime.

---

## CLI Usage

```bash
archiveproc extract project.zip                    # Print JSON to stdout
archiveproc extract project.zip -o result.json     # Save to file
archiveproc serve                                   # Start HTTP server
```

---

## HTTP API

Base URL: `http://localhost:8000`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/extract` | Upload archive → classified file tree JSON |
| `GET` | `/health` | Server health + per-format availability |

```bash
curl -F "file=@project.zip" http://localhost:8000/extract
```

Max file size: 200 MB.

---

## Python API

```python
from archiveproc import process
from pathlib import Path

result = process(Path("project.zip"))
```

---

## JSON Output Structure

```json
{
  "files": [
    {"path": "src/Main.java", "type": "text", "size": 109,
     "text": "public class Main {\n    ...\n}"},
    {"path": "doc/report.docx", "type": "needs_docxconv", "size": 56789},
    {"path": "img/screen.png", "type": "needs_ocr", "size": 234567},
    {"path": "data/binary.dat", "type": "unsupported", "size": 999},
    {"path": "large/dump.sql", "type": "text", "size": 10485760,
     "text_truncated": true}
  ],
  "summary": {
    "total": 5,
    "text": 2,
    "needs_docxconv": 1,
    "needs_ocr": 1,
    "unsupported": 1
  }
}
```

## File Classification

| Category | Extensions | Action |
|---|---|---|
| `text` | .java .py .js .ts .json .xml .md .sql ... | Content read inline (max 5 MB) |
| `needs_docxconv` | .docx | Pass to DocxConv |
| `needs_ocr` | .png .jpg .jpeg .webp .bmp .gif .tiff | Pass to ScreenshotProc |
| `unsupported` | .pdf .pptx .exe ... | Listed only (path + size) |

Nested archives (.zip / .rar / .7z / .tar inside the parent) are recursively
extracted in-place and do not appear in the output.
