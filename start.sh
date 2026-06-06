#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "  Teaching Evaluation System Launcher"
echo "============================================"
echo ""

# === AI Service ===
echo "[1/3] Preparing AI Service..."
cd "$ROOT/ai-service"
if [ ! -d ".venv" ]; then
    echo "  Creating virtual environment..."
    python3 -m venv .venv
    source .venv/bin/activate
    pip install -r requirements.txt
    pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
fi

# === Launch all services in background ===
echo "[2/3] Starting services..."

cd "$ROOT/ai-service" && source .venv/bin/activate && python -m docxconv serve --host 0.0.0.0 --port 8000 &
PID_AI=$!

cd "$ROOT" && mvn spring-boot:run &
PID_BOOT=$!

cd "$ROOT/frontend" && npm run dev &
PID_FRONT=$!

echo "[3/3] All services launched!"
echo ""
echo "  AI Service : http://localhost:8000  (PID: $PID_AI)"
echo "  Spring Boot: http://localhost:8080  (PID: $PID_BOOT)"
echo "  Frontend   : http://localhost:5173  (PID: $PID_FRONT)"
echo ""
echo "  Press Ctrl+C to stop all services."
echo "============================================"

trap 'echo "Stopping all services..."; kill $PID_AI $PID_BOOT $PID_FRONT 2>/dev/null; exit' INT TERM
wait
