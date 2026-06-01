from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from archiveproc.extractors import process


def cmd_extract(args):
    path = Path(args.file)
    if not path.exists():
        print(f"Error: file not found: {path}", file=sys.stderr)
        sys.exit(1)

    result = process(path)
    text = json.dumps(result, ensure_ascii=False, indent=2)

    if args.output:
        Path(args.output).write_text(text, encoding="utf-8")
        print(f"Saved to {args.output}")
    else:
        print(text)


def cmd_serve(args):
    import uvicorn
    uvicorn.run("archiveproc.server:app", host=args.host, port=args.port)


def main():
    parser = argparse.ArgumentParser(
        description="ArchiveProc — Extract archives and return a classified file tree",
    )
    sub = parser.add_subparsers(dest="command")

    p_extract = sub.add_parser("extract", help="Extract an archive → JSON")
    p_extract.add_argument("file", help="Archive file to extract")
    p_extract.add_argument("-o", "--output", help="Output JSON file (default: stdout)")

    p_serve = sub.add_parser("serve", help="Start HTTP server")
    p_serve.add_argument("--host", default="0.0.0.0")
    p_serve.add_argument("--port", type=int, default=8000)

    # Legacy: positional file path without subcommand → extract
    if len(sys.argv) > 1 and sys.argv[1] not in (
        "extract", "serve", "-h", "--help",
    ):
        sys.argv.insert(1, "extract")

    args = parser.parse_args()

    if args.command == "extract":
        cmd_extract(args)
    elif args.command == "serve":
        cmd_serve(args)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
