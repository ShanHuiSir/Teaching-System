#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "  Teaching Evaluation System Launcher"
echo "============================================"
echo ""

# === Port-based kill helper ===
kill_port() {
    local port=$1
    local pid
    # Try lsof first, then ss, then fuser
    if command -v lsof &>/dev/null; then
        pid=$(lsof -ti :"$port" 2>/dev/null || true)
    elif command -v ss &>/dev/null; then
        pid=$(ss -tlnp "sport = :$port" 2>/dev/null | grep -oP 'pid=\K\d+' || true)
    elif command -v fuser &>/dev/null; then
        pid=$(fuser "$port/tcp" 2>/dev/null || true)
    fi
    if [ -n "$pid" ]; then
        echo "  Killing PID $pid on port $port..."
        kill "$pid" 2>/dev/null || true
        sleep 1
        kill -9 "$pid" 2>/dev/null || true
    else
        echo "  No process listening on port $port."
    fi
}

# === AI Service venv ===
echo "[*] Preparing AI Service..."
cd "$ROOT/ai-service"
if [ ! -d ".venv" ]; then
    echo "  Creating virtual environment..."
    python3 -m venv .venv
    source .venv/bin/activate
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
fi

# === Service launchers ===
start_ai() {
    echo "  Starting AI Service on port 8000..."
    cd "$ROOT/ai-service"
    source .venv/bin/activate
    python -m docxconv serve --host 0.0.0.0 --port 8000 &
    PID_AI=$!
    echo "  AI Service started (PID $PID_AI)"
}

start_boot() {
    echo "  Starting Spring Boot on port 8080..."
    cd "$ROOT"
    mvn spring-boot:run &
    PID_BOOT=$!
    echo "  Spring Boot started (PID $PID_BOOT)"
}

start_frontend() {
    echo "  Starting Frontend on port 5173..."
    cd "$ROOT/frontend"
    npm run dev &
    PID_FRONT=$!
    echo "  Frontend started (PID $PID_FRONT)"
}

stop_all() {
    echo ""
    echo "[*] Stopping all services..."
    kill_port 8000
    kill_port 8080
    kill_port 5173
    echo "[*] All services stopped. Goodbye!"
}

# === Launch all ===
echo "[*] Starting all services..."
start_ai
start_boot
start_frontend
echo ""
echo "  AI Service : http://localhost:8000  (PID: $PID_AI)"
echo "  Spring Boot: http://localhost:8080  (PID: $PID_BOOT)"
echo "  Frontend   : http://localhost:5173  (PID: $PID_FRONT)"
echo ""
echo "  Use the menu below to restart individual services."
echo "  Press Ctrl+C or choose option 4 to stop all services."

# === Cleanup on Ctrl+C ===
trap 'stop_all; exit 0' INT TERM

# === Interactive menu ===
while true; do
    echo ""
    echo "============================================"
    echo "  1. Restart AI Service   (port 8000)"
    echo "  2. Restart Spring Boot  (port 8080)"
    echo "  3. Restart Frontend     (port 5173)"
    echo "  4. Stop all and exit"
    echo "============================================"
    read -r -p "Enter choice [1-4]: " choice

    case "$choice" in
        1)
            echo ""
            echo "---- Restarting AI Service ----"
            kill_port 8000
            sleep 2
            start_ai
            echo "---- AI Service restarted ----"
            ;;
        2)
            echo ""
            echo "---- Restarting Spring Boot ----"
            kill_port 8080
            sleep 3
            start_boot
            echo "---- Spring Boot restarted ----"
            ;;
        3)
            echo ""
            echo "---- Restarting Frontend ----"
            kill_port 5173
            sleep 2
            start_frontend
            echo "---- Frontend restarted ----"
            ;;
        4)
            stop_all
            exit 0
            ;;
        *)
            echo "Invalid choice, try again."
            ;;
    esac
done
