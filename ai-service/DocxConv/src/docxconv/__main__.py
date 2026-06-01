import argparse
import sys
from pathlib import Path


def cmd_to_images(args):
    docx_path = Path(args.input)
    if not docx_path.exists():
        print(f"Error: file not found: {docx_path}", file=sys.stderr)
        sys.exit(1)

    output_dir = Path(args.output)

    if args.pipeline == "pandoc":
        from docxconv.converters.pandoc_img import convert as pandoc_convert

        annotate = getattr(args, "annotate", None)
        print(f"Pipeline: Pandoc + WeasyPrint (annotate={annotate or 'none'})")
        img_paths = pandoc_convert(docx_path, output_dir, dpi=args.dpi, annotate=annotate)
    else:
        from docxconv.converters.img import convert, find_soffice

        if args.soffice:
            import docxconv.converters.img as mod
            mod.find_soffice = lambda: args.soffice

        print("Pipeline: LibreOffice + PyMuPDF")
        img_paths = convert(docx_path, output_dir, dpi=args.dpi)

    for p in img_paths:
        print(f"  -> {p}")

    print(f"Done! {len(img_paths)} page(s) converted.")


def cmd_to_json(args):
    from docxconv.converters.json import convert as to_json

    if not args.input.exists():
        print(f"Error: file not found: {args.input}", file=sys.stderr)
        sys.exit(1)

    result = to_json(args.input)
    output_path = args.output or args.input.with_suffix(".json")

    if args.stdout:
        print(result)
    else:
        output_path.write_text(result, encoding="utf-8")
        print(f"Saved to {output_path}")


def cmd_extract_images(args):
    from docxconv.converters.extract_images import extract_images_metadata, extract_images_to_zip

    path = Path(args.input)
    if not path.exists():
        print(f"Error: file not found: {path}", file=sys.stderr)
        sys.exit(1)

    if args.metadata_only:
        import json
        meta = extract_images_metadata(path)
        print(json.dumps(meta, ensure_ascii=False, indent=2))
    else:
        output_dir = Path(args.output)
        zip_path = extract_images_to_zip(path, output_dir)
        print(f"Extracted {zip_path}")


def cmd_serve(args):
    import uvicorn
    uvicorn.run("docxconv.server:app", host=args.host, port=args.port)


def main():
    parser = argparse.ArgumentParser(
        description="DocxConv — Convert DOCX to images or hierarchical JSON",
    )
    sub = parser.add_subparsers(dest="command")

    # to-images
    p_img = sub.add_parser("to-images", aliases=["img"], help="Convert DOCX to images")
    p_img.add_argument("input", help="Input .docx file")
    p_img.add_argument("-o", "--output", default="./output", help="Output directory (default: ./output)")
    p_img.add_argument("--dpi", type=int, default=200, help="Image DPI (default: 200)")
    p_img.add_argument("--soffice", default=None, help="Path to soffice.exe (LibreOffice pipeline only)")
    p_img.add_argument("--pipeline", choices=["libreoffice", "pandoc"], default="libreoffice",
                       help="Conversion pipeline (default: libreoffice)")
    p_img.add_argument("--annotate", choices=["text", "blocks"], default=None,
                       help="Annotation mode (pandoc pipeline only)")

    # to-json
    p_json = sub.add_parser("to-json", aliases=["json"], help="Convert DOCX to hierarchical JSON")
    p_json.add_argument("input", type=Path, nargs="?", help="Input .docx file")
    p_json.add_argument("-o", "--output", type=Path, help="Output .json file (default: <input_stem>.json)")
    p_json.add_argument("--stdout", action="store_true", help="Print JSON to stdout")

    # extract-images
    p_extract = sub.add_parser("extract-images", aliases=["extract"], help="Extract embedded images from DOCX/PDF")
    p_extract.add_argument("input", help="Input .docx or .pdf file")
    p_extract.add_argument("-o", "--output", default="./output", help="Output directory (default: ./output)")
    p_extract.add_argument("--metadata-only", action="store_true", help="Print metadata JSON to stdout")

    # serve
    p_serve = sub.add_parser("serve", help="Start HTTP server")
    p_serve.add_argument("--host", default="0.0.0.0")
    p_serve.add_argument("--port", type=int, default=8000)

    # Legacy: positional file path without subcommand -> to-json
    if len(sys.argv) > 1 and sys.argv[1] not in (
        "to-images", "img", "to-json", "json", "extract-images", "extract",
        "serve", "-h", "--help",
    ):
        sys.argv.insert(1, "json")

    args = parser.parse_args()

    if args.command in ("to-images", "img"):
        cmd_to_images(args)
    elif args.command in ("to-json", "json"):
        if not args.input:
            p_json.print_help()
            sys.exit(1)
        cmd_to_json(args)
    elif args.command in ("extract-images", "extract"):
        cmd_extract_images(args)
    elif args.command == "serve":
        cmd_serve(args)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
