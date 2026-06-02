"""Pandoc + WeasyPrint pipeline: DOCX → HTML → (annotate) → PDF → images.

Provides two annotation modes on top of plain conversion:
  ``text``   – keep text, add coloured borders / labels per element type
  ``blocks`` – replace text with colour blocks (hide all content)
"""

from __future__ import annotations

import shutil
import subprocess
from pathlib import Path
from typing import Optional

from docxconv.converters.img import find_soffice  # not used here, kept for symmetry

# ---------------------------------------------------------------------------
# Pandoc: DOCX → HTML
# ---------------------------------------------------------------------------


def _find_pandoc() -> str:
    found = shutil.which("pandoc") or shutil.which("pandoc.exe")
    if found:
        return found
    raise RuntimeError(
        "pandoc not found on PATH. Install with: scoop install pandoc"
    )


def docx_to_html(docx_path: Path) -> str:
    """Convert a DOCX file to self-contained HTML via pandoc."""
    pandoc = _find_pandoc()
    result = subprocess.run(
        [
            pandoc,
            str(docx_path.resolve()),
            "--from", "docx",
            "--to", "html5",
            "--standalone",
            "--embed-resources",
            "--wrap=none",
        ],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    if result.returncode != 0:
        raise RuntimeError(f"Pandoc failed:\n{result.stderr}")
    return result.stdout


# ---------------------------------------------------------------------------
# Annotation CSS
# ---------------------------------------------------------------------------

_TEXT_CSS = """
<style id="docxconv-annotation">
  /* ---- text mode: keep content, add structure markers ---- */
  body { font-family: "Noto Sans", "Microsoft YaHei", sans-serif; }

  h1 {
    border-left: 6px solid #e74c3c; padding-left: 12px;
    background: #fdedec; position: relative;
  }
  h1::before { content: "[H1] "; color: #e74c3c; font-size: 0.6em; }

  h2 {
    border-left: 5px solid #e67e22; padding-left: 10px;
    background: #fef5e7;
  }
  h2::before { content: "[H2] "; color: #e67e22; font-size: 0.65em; }

  h3 {
    border-left: 4px solid #f1c40f; padding-left: 8px;
    background: #fef9e7;
  }
  h3::before { content: "[H3] "; color: #cba90e; font-size: 0.7em; }

  h4, h5, h6 {
    border-left: 3px solid #f39c12; padding-left: 6px;
    background: #fef9e7;
  }

  table {
    border: 2px dashed #3498db !important;
    border-collapse: collapse;
  }
  table td, table th {
    border: 1px solid #3498db !important; padding: 4px 8px;
  }
  table thead { background: #ebf5fb; }

  blockquote {
    border-left: 5px solid #27ae60; padding-left: 12px;
    background: #eafaf1; color: #1e8449;
  }

  pre, code { background: #f0f0f0; border-radius: 3px; }
  pre { border-left: 4px solid #7f8c8d; padding: 8px; }

  ul, ol { padding-left: 24px; }
  ul > li { list-style-type: disc; }
  ol > li { list-style-type: decimal; }
</style>
"""

_BLOCKS_CSS = """
<style id="docxconv-annotation">
  /* ---- blocks mode: hide text, preserve structure + whitespace ---- */

  /* Hide text everywhere but keep element boxes intact */
  body, div, p, h1, h2, h3, h4, h5, h6,
  blockquote, pre, code, li, td, th, span, a, em, strong, b, i, u {
    color: transparent !important;
    text-shadow: none !important;
  }

  body {
    font-family: sans-serif; background: #fff;
    line-height: 1.5;
  }

  /* Ensure block-level spacing is preserved */
  h1, h2, h3, h4, h5, h6, p, blockquote, pre, ul, ol, li, hr {
    display: block;
    margin-left: 0;
    margin-right: 0;
  }

  h1 { background: #e74c3c !important; margin-top: 0.67em; margin-bottom: 0.67em; min-height: 24pt; line-height: 24pt; }
  h2 { background: #e67e22 !important; margin-top: 0.83em; margin-bottom: 0.83em; min-height: 20pt; line-height: 20pt; }
  h3 { background: #f1c40f !important; margin-top: 1em;   margin-bottom: 1em;   min-height: 16pt; line-height: 16pt; }
  h4, h5, h6 { background: #f39c12 !important; margin-top: 1.33em; margin-bottom: 1.33em; min-height: 14pt; line-height: 14pt; }

  p {
    background: #e8e8e8 !important;
    margin-top: 1em; margin-bottom: 1em;
    min-height: 12pt; line-height: 12pt;
  }
  /* Empty / whitespace-only paragraphs must still take space */
  p:empty, p:blank {
    min-height: 12pt; line-height: 12pt;
    background: #e0e0e0 !important;
  }

  table {
    background: #d6eaf8 !important;
    border: 2px solid #2980b9 !important;
    border-collapse: collapse;
  }
  table td, table th {
    color: transparent !important;
    background: #aed6f1 !important;
    border: 1px solid #2980b9 !important;
    min-width: 30px; min-height: 14pt; line-height: 14pt;
    padding: 4px 8px;
  }

  blockquote {
    background: #a9dfbf !important;
    border-left: 5px solid #1e8449 !important;
    margin: 1em 0; padding: 0.5em 1em;
    min-height: 14pt; line-height: 14pt;
  }

  pre { background: #bdc3c7 !important; margin: 1em 0; padding: 8px; min-height: 14pt; line-height: 14pt; }
  code { background: #bdc3c7 !important; }

  ul, ol { padding-left: 24px; margin: 1em 0; }
  ul > li { background: #f5f5f5 !important; margin-bottom: 2px; min-height: 12pt; line-height: 12pt; }
  ol > li { background: #f0f0f0 !important; margin-bottom: 2px; min-height: 12pt; line-height: 12pt; }

  /* Divider lines preserved */
  hr { border: 1px solid #ccc; margin: 1em 0; }

  /* Hidden: pure-decoration elements */
  img, svg { display: none; }
</style>
"""

_CSS_MAP = {"text": _TEXT_CSS, "blocks": _BLOCKS_CSS}


def annotate_html(html: str, mode: str) -> str:
    """Inject annotation CSS into HTML <head>."""
    css = _CSS_MAP.get(mode)
    if css is None:
        raise ValueError(f"Unknown annotation mode: {mode!r}. Use 'text' or 'blocks'.")
    if "</head>" in html:
        return html.replace("</head>", f"{css}\n</head>", 1)
    # No <head> — prepend
    return css + "\n" + html


# ---------------------------------------------------------------------------
# WeasyPrint: HTML → PDF
# ---------------------------------------------------------------------------


def html_to_pdf(html: str, output_dir: Path, stem: str) -> Path:
    """Render HTML to PDF. Tries WeasyPrint first, falls back to Playwright."""
    output_dir.mkdir(parents=True, exist_ok=True)
    pdf_path = output_dir / f"{stem}.pdf"

    # 1) WeasyPrint (requires GTK3 — available on Linux, may fail on Windows)
    try:
        import io, os, sys
        _stderr_save = sys.stderr
        sys.stderr = io.StringIO()  # suppress GTK3 warning noise on Windows
        try:
            from weasyprint import HTML
        finally:
            sys.stderr = _stderr_save
        HTML(string=html).write_pdf(str(pdf_path))
        if pdf_path.exists():
            return pdf_path
    except OSError:
        pass  # GTK3 not available, try fallback

    # 2) Playwright fallback (cross-platform, headless Chromium)
    try:
        _playwright_pdf(html, pdf_path)
        if pdf_path.exists():
            return pdf_path
    except Exception as e2:
        raise RuntimeError(
            "No HTML→PDF engine available. "
            "WeasyPrint needs GTK3. Playwright error: " + str(e2)
        )


def _playwright_pdf(html: str, pdf_path: Path) -> None:
    from playwright.sync_api import sync_playwright

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        page.set_content(html, wait_until="networkidle")
        page.pdf(path=str(pdf_path), print_background=True)
        browser.close()


# ---------------------------------------------------------------------------
# Full pipeline
# ---------------------------------------------------------------------------


def convert(
    docx_path: Path,
    output_dir: Path,
    *,
    dpi: int = 200,
    annotate: Optional[str] = None,
) -> list[Path]:
    """DOCX → Pandoc → HTML → [annotate] → WeasyPrint → PDF → PyMuPDF → images.

    Parameters
    ----------
    annotate:
        ``None`` – plain conversion.
        ``"text"`` – keep text, add structural markers.
        ``"blocks"`` – replace text with colour blocks.
    """
    from docxconv.converters.img import pdf_to_images

    html = docx_to_html(docx_path)
    if annotate:
        html = annotate_html(html, annotate)
    pdf_path = html_to_pdf(html, output_dir, docx_path.stem)
    img_dir = output_dir / "images"
    return pdf_to_images(pdf_path, img_dir, dpi=dpi)
