from __future__ import annotations

import re
from datetime import datetime
from pathlib import Path

from PIL import Image

from screenshotproc.ocr import ocr_image


__all__ = ["ocr_image", "ocr_images"]

# ---------------------------------------------------------------------------
# Timestamp extraction for multi-image ordering
# ---------------------------------------------------------------------------

_TS_PATTERNS = [
    re.compile(r"(\d{4}-\d{2}-\d{2}[_T]\d{2}[-:]\d{2}[-:]\d{2})"),  # 2026-05-31_16-49-40
    re.compile(r"(\d{8}[_ ]\d{6})"),  # 20260531_164940
    re.compile(r"(\d{14})"),  # 20260531164940
]


def _extract_timestamp(filename: str) -> datetime | None:
    for pat in _TS_PATTERNS:
        m = pat.search(filename)
        if m:
            ts_str = m.group(1)
            for fmt in (
                "%Y-%m-%d_%H-%M-%S",
                "%Y-%m-%d_%H:%M:%S",
                "%Y-%m-%dT%H:%M:%S",
                "%Y%m%d_%H%M%S",
                "%Y%m%d %H%M%S",
                "%Y%m%d%H%M%S",
            ):
                try:
                    return datetime.strptime(ts_str, fmt)
                except ValueError:
                    continue
    return None


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------


def ocr_images(
    paths: list[Path],
    filenames: list[str] | None = None,
) -> dict:
    """OCR one or more images and return a structured JSON-serializable dict.

    Images are sorted by:
      1. Timestamp extracted from filename
      2. Original position for files without a parseable timestamp
      3. Alphabetical filename for ties
    """
    if filenames and len(filenames) != len(paths):
        raise ValueError("filenames length must match paths length")

    # Attach metadata to each entry
    entries: list[dict] = []
    for i, p in enumerate(paths):
        fname = filenames[i] if filenames else p.name
        entries.append(
            {
                "path": p,
                "filename": fname,
                "timestamp": _extract_timestamp(fname),
                "original_index": i,
            }
        )

    # Sort: timestamp-aware first, then original_index for untimestamped, then filename
    def _sort_key(e: dict) -> tuple[int, datetime, int, str]:
        has_ts = 0 if e["timestamp"] is not None else 1
        ts = e["timestamp"] or datetime.min
        return (has_ts, ts, e["original_index"], e["filename"])

    entries.sort(key=_sort_key)

    images: list[dict] = []
    for idx, entry in enumerate(entries, start=1):
        img = Image.open(entry["path"])
        lines = ocr_image(img)
        images.append(
            {
                "index": idx,
                "file": entry["filename"],
                "width": img.width,
                "height": img.height,
                "lines": lines,
            }
        )

    return {"images": images}
