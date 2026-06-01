"""Extract embedded images from DOCX and PDF files.

Returns a list of :class:`ImageInfo` dataclass instances, each carrying the
raw image bytes plus positional / contextual metadata.
"""

from __future__ import annotations

import io
import json
import zipfile
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from PIL import Image

# ---------------------------------------------------------------------------
# Data class
# ---------------------------------------------------------------------------


@dataclass
class ImageInfo:
    index: int
    filename: str
    format: str  # "png", "jpeg", etc.
    width: int
    height: int
    raw_bytes: bytes
    page: int | None = None
    bbox: list[float] | None = None
    context: str | None = None

    # Not serialised — handled separately in the ZIP
    _metadata_only: bool = field(default=False, repr=False)

    def to_metadata(self) -> dict[str, Any]:
        return {
            "index": self.index,
            "file": self.filename,
            "format": self.format,
            "width": self.width,
            "height": self.height,
            "page": self.page,
            "bbox": self.bbox,
            "context": self.context,
        }


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

CONTEXT_MAX_CHARS = 200
QUALITY_THRESHOLD = 0.5  # skip images smaller than this fraction of page area


def _sniff_format(raw: bytes, fallback: str) -> str:
    """Determine image format from magic bytes."""
    if raw[:8] == b"\x89PNG\r\n\x1a\n":
        return "png"
    if raw[:2] == b"\xff\xd8":
        return "jpeg"
    if raw[:4] == b"GIF8":
        return "gif"
    if raw[:4] == b"RIFF" and raw[8:12] == b"WEBP":
        return "webp"
    if raw[:2] in (b"BM",):
        return "bmp"
    return fallback


def _sanitise_filename(fmt: str, index: int) -> str:
    ext = {"jpeg": "jpg"}.get(fmt, fmt)
    return f"image_{index:03d}.{ext}"


# ---------------------------------------------------------------------------
# DOCX
# ---------------------------------------------------------------------------

_DOCX_NS = {
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
    "wp14": "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing",
}


def _extract_from_docx(path: Path) -> list[ImageInfo]:
    from docx import Document
    from lxml import etree

    doc = Document(str(path))
    seen: dict[str, int] = {}  # partname → index
    result: list[ImageInfo] = []
    index = 0
    rels = doc.part.rels

    # Method 1: inline shapes
    for shape in doc.inline_shapes:
        try:
            blip = shape._inline.graphic.graphicData.pic.blipFill.blip
            rId = blip.embed
            img_part = rels[rId].target_part
        except Exception:
            continue
        if img_part.partname in seen:
            continue
        index += 1
        raw = img_part.blob
        fmt = _sniff_format(raw, img_part.partname.rsplit(".", 1)[-1])
        info = ImageInfo(
            index=index,
            filename=_sanitise_filename(fmt, index),
            format=fmt,
            width=0,
            height=0,
            raw_bytes=raw,
            context=_inline_shape_context(shape),
        )
        info.width, info.height = _image_dims(raw)
        result.append(info)
        seen[img_part.partname] = index

    # Method 2: paragraph-level XML for anchored / floating images
    for para in doc.paragraphs:
        try:
            root = etree.fromstring(para._p.xml)
        except Exception:
            continue
        for blip in root.iter(f"{{{_DOCX_NS['a']}}}blip"):
            embed = blip.attrib.get(f"{{{_DOCX_NS['r']}}}embed")
            if embed is None:
                continue
            try:
                img_part = rels[embed].target_part
            except Exception:
                continue
            if img_part.partname in seen:
                continue
            index += 1
            raw = img_part.blob
            fmt = _sniff_format(raw, img_part.partname.rsplit(".", 1)[-1])
            para_text = para.text.strip()[:CONTEXT_MAX_CHARS] if para.text else None
            info = ImageInfo(
                index=index,
                filename=_sanitise_filename(fmt, index),
                format=fmt,
                width=0,
                height=0,
                raw_bytes=raw,
                context=para_text,
            )
            info.width, info.height = _image_dims(raw)
            result.append(info)
            seen[img_part.partname] = index

    return result


def _inline_shape_context(shape) -> str | None:
    """Try to get text from paragraph(s) surrounding this inline shape."""
    try:
        # shape._inline is <wp:inline>, parent chain:
        #   wp:inline → w:drawing → w:r → w:p
        p_elem = shape._inline.getparent().getparent().getparent()

        def _para_text(elem) -> str:
            parts = []
            for t in elem.iter(f"{{{_DOCX_NS['w']}}}t"):
                if t.text:
                    parts.append(t.text)
            return "".join(parts).strip()

        # Try current paragraph first
        text = _para_text(p_elem)
        if text:
            return text[:CONTEXT_MAX_CHARS]

        # Try previous sibling paragraph
        prev = p_elem.getprevious()
        while prev is not None:
            if prev.tag == f"{{{_DOCX_NS['w']}}}p":
                t = _para_text(prev)
                if t:
                    return t[:CONTEXT_MAX_CHARS]
            prev = prev.getprevious()

        # Try next sibling paragraph
        nxt = p_elem.getnext()
        while nxt is not None:
            if nxt.tag == f"{{{_DOCX_NS['w']}}}p":
                t = _para_text(nxt)
                if t:
                    return t[:CONTEXT_MAX_CHARS]
            nxt = nxt.getnext()

        return None
    except Exception:
        return None


def _image_dims(raw: bytes) -> tuple[int, int]:
    try:
        img = Image.open(io.BytesIO(raw))
        return img.size
    except Exception:
        return 0, 0


# ---------------------------------------------------------------------------
# PDF
# ---------------------------------------------------------------------------


def _extract_from_pdf(path: Path) -> list[ImageInfo]:
    import fitz

    doc = fitz.open(str(path))
    result: list[ImageInfo] = []
    index = 0

    for page_num, page in enumerate(doc, start=1):
        page_area = page.rect.width * page.rect.height

        for img in page.get_images(full=True):
            xref = img[0]
            rects = page.get_image_rects(xref)
            if not rects:
                continue

            # Skip tiny images (icons, decorations)
            for rect in rects:
                area = rect.width * rect.height
                if page_area > 0 and area / page_area < QUALITY_THRESHOLD / 100:
                    continue

            base = doc.extract_image(xref)
            raw = base["image"]
            fmt = base["ext"]
            w, h = base["width"], base["height"]

            # Collect surrounding text
            context = _pdf_nearby_text(page, rects[0] if rects else None)
            bbox = [rects[0].x0, rects[0].y0, rects[0].x1, rects[0].y1] if rects else None

            index += 1
            result.append(
                ImageInfo(
                    index=index,
                    filename=_sanitise_filename(fmt, index),
                    format=fmt,
                    width=w,
                    height=h,
                    raw_bytes=raw,
                    page=page_num,
                    bbox=bbox,
                    context=context,
                )
            )

    doc.close()
    return result


def _pdf_nearby_text(page, rect, margin: float = 50) -> str | None:
    if rect is None:
        return None
    try:
        expanded = rect + (-margin, -margin, margin, margin)
        blocks = page.get_text("blocks", clip=expanded)
        lines = [b[4].strip() for b in blocks if b[4].strip()]
        return "\n".join(lines)[:CONTEXT_MAX_CHARS] if lines else None
    except Exception:
        return None


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------


def extract_images(path: Path) -> list[ImageInfo]:
    """Extract embedded images from a DOCX or PDF file."""
    s = path.suffix.lower()
    if s == ".docx":
        return _extract_from_docx(path)
    if s == ".pdf":
        return _extract_from_pdf(path)
    raise ValueError(f"Unsupported format: {s}. Expected .docx or .pdf")


def extract_images_to_zip(path: Path, output_dir: Path) -> Path:
    """Extract images and package as a ZIP with metadata.json inside.

    Returns the path to the created ZIP file.
    """
    images = extract_images(path)

    output_dir.mkdir(parents=True, exist_ok=True)
    zip_path = output_dir / f"{path.stem}_images.zip"

    metadata: dict[str, Any] = {"source": path.name, "total_images": len(images), "images": []}

    with zipfile.ZipFile(zip_path, "w") as zf:
        for info in images:
            zf.writestr(f"images/{info.filename}", info.raw_bytes)
            metadata["images"].append(info.to_metadata())

        zf.writestr("metadata.json", json.dumps(metadata, ensure_ascii=False, indent=2))

    return zip_path


def extract_images_metadata(path: Path) -> dict[str, Any]:
    """Return metadata dict only (no image bytes)."""
    images = extract_images(path)
    return {
        "source": path.name,
        "total_images": len(images),
        "images": [img.to_metadata() for img in images],
    }
