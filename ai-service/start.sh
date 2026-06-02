#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

if [ ! -d ".venv" ]; then
    python3 -m venv .venv
    source .venv/bin/activate
    pip install --upgrade pip
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ArchiveProc -e ScreenshotProc
else
    source .venv/bin/activate
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ArchiveProc -e ScreenshotProc
fi

python -m docxconv serve --host 0.0.0.0 --port 8000
