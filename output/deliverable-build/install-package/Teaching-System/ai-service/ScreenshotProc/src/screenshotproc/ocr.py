from __future__ import annotations

import threading
from typing import Any, Optional

import numpy as np
from PIL import Image, ImageOps

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

CONF_DROP = 0.15   # below this: pure noise, discard
CONF_NOISY = 0.5   # below this: mark noisy (AI should treat with caution)
DARK_THEME_RATIO = 0.4  # if >40% of pixels are dark, treat as dark theme

# ---------------------------------------------------------------------------
# Lazy reader singleton
# ---------------------------------------------------------------------------

_lock = threading.Lock()
_reader: Optional[Any] = None


def _load_reader():
    global _reader
    if _reader is not None:
        return
    with _lock:
        if _reader is not None:
            return
        import easyocr

        _reader = easyocr.Reader(["ch_sim", "en"], gpu=False, verbose=False)


# ---------------------------------------------------------------------------
# Preprocessing
# ---------------------------------------------------------------------------


def _is_dark_theme(arr: np.ndarray) -> bool:
    gray = np.mean(arr, axis=2)
    return bool((gray < 80).mean() > DARK_THEME_RATIO)


def _preprocess(image: Image.Image) -> Image.Image:
    """Normalize image for OCR: convert to RGB, invert dark themes."""
    image = image.convert("RGB")
    arr = np.array(image)
    if _is_dark_theme(arr):
        image = ImageOps.invert(image)
    return image


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------


def ocr_image(image: Image.Image) -> list[dict]:
    """Run OCR on a single PIL Image.

    Returns a list of dicts sorted in reading order::

        {"text": str, "bbox": [x1, y1, x2, y2], "confidence": float, "noisy": bool}
    """
    _load_reader()

    image = _preprocess(image)
    arr = np.array(image)

    raw: list[tuple] = _reader.readtext(arr)

    lines: list[dict] = []
    for bbox, text, conf in raw:
        text = (text or "").strip()
        if not text:
            continue
        c = float(conf)
        if c < CONF_DROP:
            continue

        xs = [p[0] for p in bbox]
        ys = [p[1] for p in bbox]
        lines.append(
            {
                "text": text,
                "bbox": [round(float(min(xs)), 1), round(float(min(ys)), 1),
                         round(float(max(xs)), 1), round(float(max(ys)), 1)],
                "confidence": round(c, 4),
                "noisy": bool(c < CONF_NOISY),
            }
        )

    lines.sort(key=_reading_order_key)
    return lines


def _reading_order_key(line: dict) -> tuple[float, float]:
    bbox = line.get("bbox", [0, 0, 0, 0])
    y = bbox[1] if len(bbox) > 1 else 0
    x = bbox[0] if len(bbox) > 0 else 0
    band = (y // 10) * 10
    return (band, x)
