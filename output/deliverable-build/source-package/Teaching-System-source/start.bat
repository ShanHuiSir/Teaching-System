@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"

echo ============================================
echo   Teaching Evaluation System Launcher
echo ============================================
echo.

REM === AI Service venv ===
echo [*] Preparing AI Service...
cd /d "%ROOT%ai-service"
if not exist .venv (
    echo   Creating virtual environment...
    python -m venv .venv
    call .venv\Scripts\activate.bat
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
)

REM === Launch all ===
call :launch_all

REM === Interactive menu ===
:menu
echo.
echo ============================================
echo   1. Restart AI Service   (port 8000^)
echo   2. Restart Spring Boot  (port 8080^)
echo   3. Restart Frontend     (port 5173^)
echo   4. Stop all and exit
echo ============================================
set "choice="
set /p "choice=Enter choice [1-4]: "
if "%choice%"=="1" (
    call :restart_ai
    goto menu
)
if "%choice%"=="2" (
    call :restart_boot
    goto menu
)
if "%choice%"=="3" (
    call :restart_frontend
    goto menu
)
if "%choice%"=="4" (
    call :stop_all
    goto :eof
)
echo Invalid choice, try again.
goto menu

REM =============================================
REM  Service launchers
REM =============================================

:launch_all
echo [*] Starting all services...
call :start_ai
call :start_boot
call :start_frontend
echo.
echo   AI Service : http://localhost:8000
echo   Spring Boot: http://localhost:8080
echo   Frontend   : http://localhost:5173
echo.
echo   Use this menu to restart individual services.
goto :eof

:start_ai
echo   Starting AI Service...
start "AI-Service" /d "%ROOT%ai-service" cmd /c "title AI-Service ^(8000^) && call .venv\Scripts\activate.bat && python -m docxconv serve --host 0.0.0.0 --port 8000"
goto :eof

:start_boot
echo   Starting Spring Boot...
start "Spring-Boot" /d "%ROOT%" cmd /c "title Spring-Boot ^(8080^) && mvn spring-boot:run"
goto :eof

:start_frontend
echo   Starting Frontend...
if not exist "%ROOT%frontend\node_modules" (
    echo     Installing frontend dependencies...
    cd /d "%ROOT%frontend"
    call npm install
    if not exist "%ROOT%frontend\node_modules" (
        echo     ERROR: Dependencies installed to wrong location - check npm config
        goto :eof
    )
)
start "Frontend" /d "%ROOT%frontend" cmd /c "title Frontend ^(5173^) && npm run dev"
goto :eof

REM =============================================
REM  Restart helpers
REM =============================================

:restart_ai
echo.
echo ---- Restarting AI Service ----
call :kill_port 8000
timeout /t 2 /nobreak >nul
call :start_ai
echo ---- AI Service restarted ----
goto :eof

:restart_boot
echo.
echo ---- Restarting Spring Boot ----
call :kill_port 8080
timeout /t 3 /nobreak >nul
call :start_boot
echo ---- Spring Boot restarted ----
goto :eof

:restart_frontend
echo.
echo ---- Restarting Frontend ----
call :kill_port 5173
timeout /t 2 /nobreak >nul
call :start_frontend
echo ---- Frontend restarted ----
goto :eof

REM =============================================
REM  Kill by port
REM =============================================

:kill_port
set "found="
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| find ":%1 " ^| findstr "LISTENING"') do (
    set "found=1"
    echo   Killing PID %%a on port %1...
    taskkill /F /PID %%a >nul 2>&1
)
if not defined found echo   No process listening on port %1.
goto :eof

REM =============================================
REM  Stop all
REM =============================================

:stop_all
echo.
echo [*] Stopping all services...
call :kill_port 8000
call :kill_port 8080
call :kill_port 5173
echo [*] All services stopped. Goodbye!
timeout /t 2 /nobreak >nul
goto :eof
