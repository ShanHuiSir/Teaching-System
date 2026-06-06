@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"

echo ============================================
echo   Teaching Evaluation System Launcher
echo ============================================
echo.

REM === AI Service ===
echo [1/3] Preparing AI Service...
cd /d "%ROOT%ai-service"
if not exist .venv (
    echo   Creating virtual environment...
    python -m venv .venv
    call .venv\Scripts\activate.bat
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
)

REM === Launch all services in separate windows ===
echo [2/3] Starting services in separate windows...

start "AI-Service (8000)" /d "%ROOT%ai-service" cmd /k "call .venv\Scripts\activate.bat && python -m docxconv serve --host 0.0.0.0 --port 8000"
start "Spring-Boot (8080)" /d "%ROOT%" cmd /k "mvn spring-boot:run"
start "Frontend (5173)" /d "%ROOT%frontend" cmd /k "npm run dev"

echo [3/3] All services launched!
echo.
echo   AI Service : http://localhost:8000
echo   Spring Boot: http://localhost:8080
echo   Frontend   : http://localhost:5173
echo.
echo   Close each window to stop its service.
echo ============================================
pause
