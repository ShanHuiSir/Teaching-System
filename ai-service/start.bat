@echo off
cd /d "%~dp0"
if not exist .venv (
    python -m venv .venv
    call .venv\Scripts\activate.bat
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
) else (
    call .venv\Scripts\activate.bat
)
python -m docxconv serve --host 0.0.0.0 --port 8000
