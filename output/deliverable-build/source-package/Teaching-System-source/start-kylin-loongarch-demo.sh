#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

BACKEND_LOG="$LOG_DIR/kylin-backend.log"
FRONTEND_LOG="$LOG_DIR/kylin-frontend.log"

port_alive() {
    local port="$1"
    if command -v ss >/dev/null 2>&1; then
        ss -lnt "sport = :$port" | tail -n +2 | grep -q .
        return $?
    fi
    if command -v lsof >/dev/null 2>&1; then
        lsof -ti :"$port" >/dev/null 2>&1
        return $?
    fi
    return 1
}

kill_port() {
    local port="$1"
    if command -v fuser >/dev/null 2>&1; then
        fuser -k "$port"/tcp >/dev/null 2>&1 || true
    elif command -v lsof >/dev/null 2>&1; then
        local pids
        pids="$(lsof -ti :"$port" 2>/dev/null || true)"
        [ -n "$pids" ] && kill $pids >/dev/null 2>&1 || true
    fi
}

require_cmd() {
    local cmd="$1"
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "Missing command: $cmd"
        exit 1
    fi
}

echo "== Kylin / LoongArch demo launcher =="
echo
echo "[1/5] Environment proof"
echo "kernel: $(uname -srmo)"
echo "machine: $(uname -m)"
if [ -r /etc/os-release ]; then
    echo "os-release:"
    grep -E '^(NAME|VERSION|PRETTY_NAME)=' /etc/os-release || true
fi
echo

if [ "$(uname -m)" != "loongarch64" ]; then
    echo "WARNING: current machine is not loongarch64. For final acceptance, run this inside the LoongArch Kylin VM."
    echo
fi

echo "[2/5] Toolchain check"
require_cmd java
require_cmd mvn
require_cmd node
require_cmd npm
java -version 2>&1 | head -n 1
mvn -v | head -n 1
node -v
npm -v
echo

echo "[3/5] Stop old demo processes"
kill_port 8080
kill_port 5173
sleep 1

if port_alive 8080 || port_alive 5173; then
    echo "Port 8080 or 5173 is still occupied. Please close the old process and retry."
    exit 1
fi

echo "[4/5] Start backend with demo profile"
: > "$BACKEND_LOG"
cd "$ROOT"
nohup mvn spring-boot:run \
    -Dspring-boot.run.profiles=demo \
    >> "$BACKEND_LOG" 2>&1 &

for _ in $(seq 1 45); do
    if port_alive 8080; then
        break
    fi
    sleep 2
done

if ! port_alive 8080; then
    echo "Backend failed to start. See $BACKEND_LOG"
    tail -80 "$BACKEND_LOG" || true
    exit 1
fi
echo "Backend: http://localhost:8080"

echo "[5/5] Start frontend dev server"
: > "$FRONTEND_LOG"
cd "$ROOT/frontend"
if [ ! -d node_modules ]; then
    npm install >> "$FRONTEND_LOG" 2>&1
fi
nohup npm run dev -- --host 0.0.0.0 \
    >> "$FRONTEND_LOG" 2>&1 &

for _ in $(seq 1 30); do
    if port_alive 5173; then
        break
    fi
    sleep 1
done

if ! port_alive 5173; then
    echo "Frontend failed to start. See $FRONTEND_LOG"
    tail -80 "$FRONTEND_LOG" || true
    exit 1
fi

echo
echo "Demo is ready."
echo "Frontend:   http://localhost:5173"
echo "Backend:    http://localhost:8080/api/health"
echo "Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "Login:      teacher / 123456"
echo
echo "Logs:"
echo "Backend:  $BACKEND_LOG"
echo "Frontend: $FRONTEND_LOG"
echo
echo "Stop command:"
echo "fuser -k 8080/tcp 5173/tcp"
