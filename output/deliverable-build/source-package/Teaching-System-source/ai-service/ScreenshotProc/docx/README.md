# ScreenshotProc

Extract text from screenshots via [EasyOCR](https://github.com/JaidedAI/EasyOCR),
outputting line-level JSON with bounding-box coordinates. No semantic merging —
structure interpretation is left to downstream AI.

## Installation

```bash
uv venv --python 3.13
uv pip install -e .
```

Requires Python >= 3.13. Models (~800 MB) download automatically on first run
and are cached locally. CPU-only mode by default.

---

## CLI Usage

```
screenshotproc <subcommand> [options]
```

### Subcommands

| Command | Description |
|---|---|
| `ocr` | OCR one or more images → JSON (default) |
| `serve` | Start HTTP server |

### OCR images

```bash
screenshotproc ocr img1.png img2.png                    # Print JSON to stdout
screenshotproc ocr *.png -o result.json                 # Save to file
```

When no subcommand is given, positional file paths default to `ocr`:

```bash
screenshotproc img1.png   # same as: screenshotproc ocr img1.png
```

### Start server

```bash
screenshotproc serve                    # Default 0.0.0.0:8000
screenshotproc serve --port 8080 --host 127.0.0.1
```

---

## HTTP API

Base URL: `http://localhost:8000`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/ocr` | Upload 1-N images → line-level JSON |
| `GET` | `/health` | Server health + model status |

### `POST /ocr`

- **Body**: multipart file upload (PNG / JPEG / WebP), multiple files accepted
- **Max file size**: 50 MB per file
- **Returns**: JSON with per-image line arrays

```bash
curl -F "files=@shot1.png" -F "files=@shot2.png" http://localhost:8000/ocr
```

### `GET /health`

```bash
curl http://localhost:8000/health
# {"status":"ok","models_loaded":true}
```

---

## Python API

```python
from screenshotproc import ocr_image, ocr_images
from pathlib import Path

# Single image
lines = ocr_image(Image.open("screenshot.png"))

# Multiple images (with timestamp-aware sorting)
result = ocr_images([Path("a.png"), Path("b.png")])
```

---

## JSON Output Structure

```json
{
  "images": [
    {
      "index": 1,
      "file": "Clip_2026-05-31_16-49-40.png",
      "width": 2560,
      "height": 1600,
      "lines": [
        {"text": "def train_model(data):", "bbox": [40, 120, 380, 142], "confidence": 0.9834, "noisy": false},
        {"text": "RRestControexer", "bbox": [80, 144, 420, 166], "confidence": 0.368, "noisy": true}
      ]
    }
  ]
}
```

- **`bbox`**: `[x1, y1, x2, y2]` in pixels
- **`lines`**: sorted in reading order (top-to-bottom, then left-to-right)
- **`confidence`**: 0–1 OCR confidence score
- **`noisy`**: `true` when confidence < 0.5 — downstream AI should treat these lines with caution
- **Preprocessing**: dark-theme screenshots (IDE, terminal) are automatically inverted before OCR
- **Filtering**: lines with confidence < 0.15 are dropped as pure noise
- Multi-image ordering: filename timestamps → upload/CLI order → alphabetical

---

## Multi-Image Ordering

Filenames are scanned for timestamps. Recognized patterns:

| Pattern | Example |
|---|---|
| `YYYY-MM-DD_HH-MM-SS` | `Clip_2026-05-31_16-49-40.png` |
| `YYYYMMDD_HHMMSS` | `screenshot_20260531_164940.png` |
| `YYYYMMDDHHMMSS` | `20260531164940.png` |

Files with timestamps sort first (chronological), followed by untimestamped files
in their original order.
