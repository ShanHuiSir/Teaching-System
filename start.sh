#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

# ── Colors ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Log files ───────────────────────────────────────────────────────────────
AI_LOG="$LOG_DIR/ai-service.log"
BOOT_LOG="$LOG_DIR/spring-boot.log"
FRONT_LOG="$LOG_DIR/frontend.log"

# ── Helper: find PID by scanning /proc for the socket inode ─────────────────
_find_pid_by_port() {
    local port=$1 hex_port inode pid
    hex_port=$(printf '%04X' "$port")
    # Extract inode from /proc/net/tcp (field 10, state 0A = LISTEN)
    inode=$(awk -v p="$hex_port" '$4=="0A" && $2~":"p"$" {print $10; exit}' /proc/net/tcp 2>/dev/null)
    [ -z "$inode" ] && return 1
    # Scan /proc/*/fd for the socket inode
    for fd_dir in /proc/[0-9]*/fd; do
        pid="${fd_dir%/fd}"; pid="${pid##*/}"
        [ "$pid" = "self" ] && continue
        if ls -l "$fd_dir" 2>/dev/null | grep -q "socket:\[$inode\]"; then
            echo "$pid"
            return 0
        fi
    done
    return 1
}

# ── Helper: kill a process by port ──────────────────────────────────────────
kill_port() {
    local port=$1 pid
    # Try multiple tools to find the PID (lsof → fuser → ss → /proc scan)
    pid=$(lsof -ti :"$port" 2>/dev/null || true)
    if [ -z "$pid" ]; then
        pid=$(fuser "$port"/tcp 2>/dev/null | grep -o '[0-9]*' | head -1 || true)
    fi
    if [ -z "$pid" ]; then
        pid=$(ss -tlnp "sport = :$port" 2>/dev/null | grep -oP 'pid=\K[0-9]+' | head -1 || true)
    fi
    if [ -z "$pid" ]; then
        pid=$(_find_pid_by_port "$port" || true)
    fi

    if [ -n "$pid" ]; then
        echo -e "  ${YELLOW}→ Killing PID $pid on port $port...${NC}"
        kill "$pid" 2>/dev/null || true
        # Wait up to 5 s for graceful shutdown
        for i in $(seq 1 5); do
            sleep 1
            if ! port_alive "$port"; then
                echo -e "    ${GREEN}✓ Port $port freed${NC}"
                return
            fi
        done
        # Still alive — force kill
        echo -e "  ${YELLOW}→ Force-killing PID $pid...${NC}"
        kill -9 "$pid" 2>/dev/null || true
        sleep 1
        if ! port_alive "$port"; then
            echo -e "    ${GREEN}✓ Port $port freed${NC}"
        else
            echo -e "    ${RED}✗ Port $port still occupied after SIGKILL — manual intervention required${NC}"
        fi
    fi
}

# ── Helper: check if a port is listening ────────────────────────────────────
port_alive() {
    local port=$1
    lsof -ti :"$port" >/dev/null 2>&1 && return 0
    fuser "$port"/tcp >/dev/null 2>&1 && return 0
    # ss always exits 0 — must verify it actually found a matching line
    [ "$(ss -tlnp "sport = :$port" 2>/dev/null | wc -l)" -gt 1 ] && return 0
    # Last resort: /proc/net/tcp (always available, no tools needed)
    local hex_port
    hex_port=$(printf '%04X' "$port")
    grep -qE ":[0-9A-F]+:${hex_port} [0-9A-F]+:0000:0000 0A" /proc/net/tcp 2>/dev/null && return 0
    return 1
}

# ── Status indicators ───────────────────────────────────────────────────────
status_dot() {
    local port=$1
    if port_alive "$port"; then
        echo -e "  ${GREEN}●${NC} port $port"
    else
        echo -e "  ${RED}○${NC} port $port"
    fi
}

# ── Ensure Python venv exists ───────────────────────────────────────────────
prepare_venv() {
    cd "$ROOT/ai-service"
    if [ ! -d ".venv" ]; then
        echo -e "  ${YELLOW}→ Creating Python virtual environment...${NC}"
        python3 -m venv .venv
        source .venv/bin/activate
        pip install -r requirements.txt -q
        pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc -q
    else
        source .venv/bin/activate
        # Quick check: is docxconv importable?
        if ! python -c "import docxconv" 2>/dev/null; then
            echo -e "  ${YELLOW}→ Installing missing packages...${NC}"
            pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc -q
        fi
    fi
}

# ── Service launchers (background, output → log files) ──────────────────────
start_ai() {
    # Ensure port is free before starting
    if port_alive 8000; then
        echo -e "    ${RED}✗ Port 8000 is still occupied — old process may still be running${NC}"
        return 1
    fi
    echo -e "  ${CYAN}→ Starting AI Service (port 8000)...${NC}"
    cd "$ROOT/ai-service"
    source .venv/bin/activate
    nohup python -m docxconv serve --host 0.0.0.0 --port 8000 \
        >> "$AI_LOG" 2>&1 &
    disown $!
    sleep 2
    if port_alive 8000; then
        echo -e "    ${GREEN}✓ AI Service started${NC}  (log: $AI_LOG)"
    else
        echo -e "    ${RED}✗ AI Service failed to start — check log: $AI_LOG${NC}"
    fi
}

start_boot() {
    echo -e "  ${CYAN}→ Compiling Spring Boot...${NC}"
    cd "$ROOT"
    if ! mvn clean compile -q >> "$BOOT_LOG" 2>&1; then
        echo -e "    ${RED}✗ Compilation failed — check log: $BOOT_LOG${NC}"
        return 1
    fi
    echo -e "    ${GREEN}✓ Compilation successful${NC}"

    # Ensure port is free before starting
    if port_alive 8080; then
        echo -e "    ${RED}✗ Port 8080 is still occupied — old process may still be running${NC}"
        return 1
    fi
    echo -e "  ${CYAN}→ Starting Spring Boot (port 8080)...${NC}"
    nohup mvn spring-boot:run >> "$BOOT_LOG" 2>&1 &
    disown $!
    # Spring Boot takes longer; wait up to 60s
    for i in $(seq 1 30); do
        sleep 2
        if port_alive 8080; then
            echo -e "    ${GREEN}✓ Spring Boot started${NC}  (log: $BOOT_LOG)"
            return
        fi
    done
    echo -e "    ${RED}✗ Spring Boot timed out — check log: $BOOT_LOG${NC}"
}

start_frontend() {
    # Ensure port is free before starting
    if port_alive 5173; then
        echo -e "    ${RED}✗ Port 5173 is still occupied — old process may still be running${NC}"
        return 1
    fi
    echo -e "  ${CYAN}→ Starting Frontend (port 5173)...${NC}"
    cd "$ROOT/frontend"
    nohup npm run dev >> "$FRONT_LOG" 2>&1 &
    disown $!
    sleep 3
    if port_alive 5173; then
        echo -e "    ${GREEN}✓ Frontend started${NC}  (log: $FRONT_LOG)"
    else
        echo -e "    ${RED}✗ Frontend failed to start — check log: $FRONT_LOG${NC}"
    fi
}

# ── Tail log helper ─────────────────────────────────────────────────────────
view_log() {
    local label=$1 file=$2
    echo ""
    echo -e "${BOLD}── ${label} (last 30 lines, Ctrl+C to return to menu) ──${NC}"
    echo -e "${YELLOW}Full log: $file${NC}"
    echo ""
    tail -30 "$file" 2>/dev/null || echo "(log file empty or missing)"
    echo ""
    echo -e "${YELLOW}── Following live output (Ctrl+C to stop) ──${NC}"
    tail -f "$file" 2>/dev/null
}

# ── Stop all ────────────────────────────────────────────────────────────────
stop_all() {
    echo ""
    echo -e "${YELLOW}[*] Stopping all services...${NC}"
    kill_port 8000
    kill_port 8080
    kill_port 5173
    echo -e "${GREEN}[*] All stopped. Goodbye!${NC}"
}

# ══════════════════════════════════════════════════════════════════════════════
#  MAIN
# ══════════════════════════════════════════════════════════════════════════════

clear
echo ""
echo -e "  ${BOLD}══════════════════════════════════════════${NC}"
echo -e "  ${BOLD}  教学评价系统 · Teaching Evaluation System${NC}"
echo -e "  ${BOLD}══════════════════════════════════════════${NC}"
echo ""

# ── Prepare ─────────────────────────────────────────────────────────────────
echo -e "${BOLD}[1/4]${NC} Preparing AI Service environment..."
prepare_venv
echo -e "  ${GREEN}✓ AI Service ready${NC}"

# ── Start all services ──────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}[2/4]${NC} Starting services..."

# Clean old logs
: > "$AI_LOG"
: > "$BOOT_LOG"
: > "$FRONT_LOG"

# Kill any leftover processes from a previous run
kill_port 8000
kill_port 8080
kill_port 5173

# Pre-flight: verify all ports are free
for p in 8000 8080 5173; do
    if port_alive "$p"; then
        echo -e "  ${RED}✗ Port $p could not be freed — free it manually and retry${NC}"
    fi
done
if port_alive 8000 || port_alive 8080 || port_alive 5173; then
    echo -e "${RED}[!] Some ports are still occupied. Please run:${NC}"
    echo -e "${YELLOW}    fuser -k 8000/tcp 8080/tcp 5173/tcp${NC}"
    exit 1
fi

start_ai
start_boot
start_frontend

# ── Summary ─────────────────────────────────────────────────────────────────
echo ""
echo -e "  ${BOLD}══════════════════════════════════════════${NC}"
echo -e "  ${BOLD}  All services launched${NC}"
echo -e "  ${BOLD}══════════════════════════════════════════${NC}"
echo ""
echo -e "  ${CYAN}Frontend   ${NC}: http://localhost:5173"
echo -e "  ${CYAN}Backend    ${NC}: http://localhost:8080"
echo -e "  ${CYAN}Swagger UI ${NC}: http://localhost:8080/swagger-ui/index.html"
echo -e "  ${CYAN}AI Service ${NC}: http://localhost:8000/docs"
echo ""
echo -e "  ${YELLOW}Login: teacher / 123456   or   temp / 123456${NC}"
echo ""
echo -e "  ${BOLD}Logs:${NC}"
echo -e "    AI Service : $AI_LOG"
echo -e "    Spring Boot: $BOOT_LOG"
echo -e "    Frontend   : $FRONT_LOG"
echo ""

# ── Cleanup on Ctrl+C ──────────────────────────────────────────────────────
trap 'stop_all; exit 0' INT TERM

# ── Interactive menu ────────────────────────────────────────────────────────
while true; do
    echo ""
    echo -e "  ${BOLD}───────────── Status ─────────────${NC}"
    status_dot 8000
    status_dot 8080
    status_dot 5173
    echo -e "  ${BOLD}─────────────────────────────────${NC}"
    echo ""
    echo "  1. View AI Service log     (tail -f)"
    echo "  2. View Spring Boot log    (tail -f)"
    echo "  3. View Frontend log       (tail -f)"
    echo "  ─────────────────────────────────"
    echo "  4. Restart AI Service      (port 8000)"
    echo "  5. Restart Spring Boot     (port 8080)"
    echo "  6. Restart Frontend        (port 5173)"
    echo "  ─────────────────────────────────"
    echo "  7. Stop all and exit"
    echo ""

    read -r -p "  Enter choice [1-7]: " choice

    case "$choice" in
        1) view_log "AI Service Log" "$AI_LOG" ;;
        2) view_log "Spring Boot Log" "$BOOT_LOG" ;;
        3) view_log "Frontend Log" "$FRONT_LOG" ;;
        4)
            echo ""
            kill_port 8000
            sleep 1
            start_ai || true
            ;;
        5)
            echo ""
            kill_port 8080
            sleep 2
            start_boot || true
            ;;
        6)
            echo ""
            kill_port 5173
            sleep 1
            start_frontend || true
            ;;
        7)
            stop_all
            exit 0
            ;;
        *)
            echo -e "  ${RED}Invalid choice, press 1-7${NC}"
            sleep 1
            ;;
    esac
done
