import shutil
import subprocess
from pathlib import Path

import fitz


def find_soffice() -> str:
    official = Path("C:/Program Files/LibreOffice/program/soffice.exe")
    if official.exists():
        return str(official)

    scoop_base = Path.home() / "scoop" / "apps" / "libreoffice"
    if scoop_base.exists():
        for ver_dir in sorted(scoop_base.iterdir(), reverse=True):
            p = ver_dir / "LibreOffice" / "program" / "soffice.exe"
            if p.exists():
                return str(p)

    found = shutil.which("soffice") or shutil.which("soffice.exe")
    if found:
        return found
    return "soffice"


def docx_to_pdf(docx_path: Path, output_dir: Path) -> Path:
    output_dir.mkdir(parents=True, exist_ok=True)
    soffice = find_soffice()

    result = subprocess.run(
        [soffice, "--headless", "--convert-to", "pdf", "--outdir",
         str(output_dir.resolve()), str(docx_path.resolve())],
        capture_output=True, text=True,
        cwd=str(Path(soffice).parent),
    )
    if result.returncode != 0:
        raise RuntimeError(f"LibreOffice conversion failed:\n{result.stderr}")

    pdf_path = output_dir / f"{docx_path.stem}.pdf"
    if not pdf_path.exists():
        raise FileNotFoundError(f"PDF not generated: {pdf_path}")
    return pdf_path


def pdf_to_images(pdf_path: Path, output_dir: Path, dpi: int = 200) -> list[Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    doc = fitz.open(str(pdf_path))
    image_paths = []
    for i, page in enumerate(doc, start=1):
        pix = page.get_pixmap(dpi=dpi)
        img_path = output_dir / f"{pdf_path.stem}_page_{i}.png"
        pix.save(str(img_path))
        image_paths.append(img_path)
    doc.close()
    return image_paths


def convert(docx_path: Path, output_dir: Path, dpi: int = 200) -> list[Path]:
    """Convert a DOCX file to images. Returns list of image paths."""
    pdf_path = docx_to_pdf(docx_path, output_dir)
    img_dir = output_dir / "images"
    return pdf_to_images(pdf_path, img_dir, dpi=dpi)
