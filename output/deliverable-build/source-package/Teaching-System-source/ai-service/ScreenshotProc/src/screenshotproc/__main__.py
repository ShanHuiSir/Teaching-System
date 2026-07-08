from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from screenshotproc import ocr_images


def cmd_ocr(args):
    paths = [Path(p) for p in args.files]
    names = [p.name for p in paths]

    missing = [p for p in paths if not p.exists()]
    if missing:
        print(f"Error: file(s) not found: {', '.join(str(p) for p in missing)}", file=sys.stderr)
        sys.exit(1)

    result = ocr_images(paths, filenames=names)
    text = json.dumps(result, ensure_ascii=False, indent=2)

    if args.output:
        Path(args.output).write_text(text, encoding="utf-8")
        print(f"Saved to {args.output}")
    else:
        print(text)


def cmd_serve(args):
    import uvicorn

    uvicorn.run("screenshotproc.server:app", host=args.host, port=args.port)


def main():
    parser = argparse.ArgumentParser(
        description="ScreenshotProc — Extract text from screenshots via EasyOCR",
    )
    sub = parser.add_subparsers(dest="command")

    p_ocr = sub.add_parser("ocr", help="OCR one or more images → JSON")
    p_ocr.add_argument("files", nargs="+", help="Image file(s) to process")
    p_ocr.add_argument("-o", "--output", help="Output JSON file (default: stdout)")

    p_serve = sub.add_parser("serve", help="Start HTTP server")
    p_serve.add_argument("--host", default="0.0.0.0")
    p_serve.add_argument("--port", type=int, default=8000)

    # Legacy: positional file paths without subcommand → ocr
    if len(sys.argv) > 1 and sys.argv[1] not in (
        "ocr", "serve", "-h", "--help",
    ):
        sys.argv.insert(1, "ocr")

    args = parser.parse_args()

    if args.command == "ocr":
        cmd_ocr(args)
    elif args.command == "serve":
        cmd_serve(args)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
