# DocxConv

Convert DOCX files to images (via LibreOffice + PyMuPDF) or hierarchical JSON.

## Installation

```bash
# Clone/download the project, then:
pip install -e .
```

Requires Python >= 3.13.

### External dependency

**Image conversion** requires LibreOffice installed. Auto-detection looks in:
- `C:\Program Files\LibreOffice\program\soffice.exe`
- `~/scoop/apps/libreoffice/`
- `PATH` (via `which soffice`)

JSON conversion has no external dependencies.

---

## CLI Usage

```
docxconv <subcommand> [options]
```

### Subcommands

| Command | Alias | Description |
|---|---|---|
| `to-images` | `img` | DOCX → PDF → PNG images |
| `to-json` | `json` | DOCX → hierarchical JSON (default) |
| `extract-images` | `extract` | DOCX/PDF → extract embedded images (ZIP + metadata) |
| `serve` | — | Start HTTP server |

### Convert to images

```bash
docxconv to-images input.docx                    # Default DPI=200, output to ./output/
docxconv to-images input.docx -o ./out --dpi 300 # Custom output dir and higher DPI
docxconv to-images input.docx --soffice /path/to/soffice.exe  # Explicit LibreOffice path
```

### Convert to JSON

```bash
docxconv to-json input.docx                      # Saves to input.json
docxconv to-json input.docx -o result.json       # Custom output path
docxconv to-json input.docx --stdout             # Print to stdout
```

When no subcommand is given, a positional file path defaults to `to-json`:

```bash
docxconv input.docx   # same as: docxconv to-json input.docx
```

### Extract embedded images

```bash
docxconv extract-images report.docx                    # → ./output/report_images.zip
docxconv extract-images slides.pdf --metadata-only     # → stdout JSON
```

Output ZIP contains `images/` folder + `metadata.json` with per-image
position, page, and surrounding text context.

### Start server

```bash
docxconv serve                    # Default 0.0.0.0:8000
docxconv serve --port 8080 --host 127.0.0.1
# or:
docxconv-serve                    # Shorthand entry point
```

---

## HTTP API

Base URL: `http://localhost:8000`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/convert/img` | Upload .docx → images (ZIP or JSON listing) |
| `POST` | `/convert/pdf` | Upload .docx → PDF |
| `POST` | `/convert/json` | Upload .docx → hierarchical JSON |
| `POST` | `/extract/images` | Upload .docx/.pdf → extract embedded images (ZIP or JSON) |
| `GET` | `/health` | Server health check |

### `/convert/img`

- **Body**: multipart file upload (`.docx`)
- **Query params**:
  - `dpi` (int, 72–600, default 200) — output image DPI
  - `format` (`zip` | `json`, default `zip`) — `zip` returns a ZIP of PNGs; `json` returns page count and filenames

### `/convert/pdf`

- **Body**: multipart file upload (`.docx`)
- **Returns**: the PDF file

### `/convert/json`

- **Body**: multipart file upload (`.docx`)
- **Returns**: JSON tree with heading hierarchy, paragraphs, lists, and tables

### Example requests

```bash
# Convert to ZIP of images
curl -F "file=@test.docx" http://localhost:8000/convert/img -o result.zip

# Convert to PDF
curl -F "file=@test.docx" http://localhost:8000/convert/pdf -o result.pdf

# Convert to JSON
curl -F "file=@test.docx" http://localhost:8000/convert/json -o result.json

# Health check
curl http://localhost:8000/health
```

---

## Python API

```python
from docxconv import to_images, docx_to_pdf, to_json, to_json_obj
from pathlib import Path

# DOCX → images (requires LibreOffice)
img_paths = to_images(Path("input.docx"), Path("./output"), dpi=200)
# Returns: [Path("output/images/input_page_1.png"), ...]

# DOCX → PDF only
pdf_path = docx_to_pdf(Path("input.docx"), Path("./output"))

# DOCX → JSON string
json_str = to_json(Path("input.docx"))

# DOCX → Python object (list of dicts)
data = to_json_obj(Path("input.docx"))
```

---

## JSON Output Structure

The JSON output is a tree representing the document's heading hierarchy:

```json
[
  {
    "level": 1,
    "heading": "Chapter 1",
    "content": [
      { "type": "paragraph", "text": "Some text..." },
      { "type": "list", "ordered": true, "items": [
          { "text": "First item" },
          { "text": "Second item" }
      ]},
      { "type": "table", "headers": ["Col A", "Col B"], "rows": [["a1", "b1"]] }
    ],
    "children": [
      {
        "level": 2,
        "heading": "Section 1.1",
        "content": [...],
        "children": [...]
      }
    ]
  }
]
```

### Recognized elements

- **Headings**: Word "Heading N" styles; also bold text with font size >= 20pt
- **Lists**: Word native numbered/bullet lists, plus manual prefixes (`1.`, `一、`, `-`, `•`, etc.)
- **Tables**: Extracted with header row and data rows
- **Paragraphs**: All other text

---

## Test Files

Located in `test/`:

| File | Description |
|---|---|
| `SimpleTest.docx` | Basic formatting — headings, paragraphs, simple list |
| `TableTest.docx` | Document with tables |
| `DifficultTest.docx` | Complex layout — nested lists, mixed content, Chinese text |

Run a quick test:

```bash
docxconv to-json test/SimpleTest.docx --stdout
docxconv to-images test/SimpleTest.docx -o ./output
```
