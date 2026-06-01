"""ai-service centralized configuration.

All values can be overridden via environment variables.
Never hardcode credentials, host/port, or model parameters in source files.
"""

from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv as _load_dotenv
_load_dotenv(Path(__file__).resolve().parent / ".env")

# ── Server ────────────────────────────────────────────────────────────

HOST = os.getenv("AI_SERVICE_HOST", "0.0.0.0")
PORT = int(os.getenv("AI_SERVICE_PORT", "8000"))
RELOAD = os.getenv("AI_SERVICE_RELOAD", "false").lower() == "true"

# ── DeepSeek / LLM ────────────────────────────────────────────────────

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_BASE_URL = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")
DEEPSEEK_TEMPERATURE = float(os.getenv("DEEPSEEK_TEMPERATURE", "0.3"))
DEEPSEEK_MAX_TOKENS = int(os.getenv("DEEPSEEK_MAX_TOKENS", "800"))

EVAL_SYSTEM_PROMPT = os.getenv("EVAL_SYSTEM_PROMPT", """\
你是一位严格但公正的教学评价专家。根据学生提交的作业内容进行评分。

评分规则：
- 满分 100 分，从 60 分基准开始
- 内容充实度（30 分）：论述深度、字数、信息量
- 结构清晰度（20 分）：章节划分、逻辑连贯、段落组织
- 格式规范性（-10~+5 分）：标题层级、排版格式等

按以下 JSON 格式输出，不要输出其他内容：
{
  "aiScore": 85,
  "aiIssues": "1. 问题一\\n2. 问题二\\n3. 问题三",
  "aiComment": "一段 50-150 字的综合评价，语气鼓励性但实事求是。"
}

注意：
- aiScore 为 0-100 的数值
- aiIssues 列出具体的问题，至少 2 条，最多 5 条，每条以 "N. " 开头，换行分隔
- aiComment 为一段 50-150 字的综合评价\
""")

# ── File type sets ────────────────────────────────────────────────────

TEXT_EXTENSIONS = frozenset(
    os.getenv("TEXT_EXTENSIONS", "").split(",") if os.getenv("TEXT_EXTENSIONS")
    else [
        ".txt", ".md", ".csv", ".log",
        ".cpp", ".c", ".h", ".hpp", ".java", ".py", ".js", ".ts",
        ".html", ".css", ".scss", ".xml", ".json", ".yaml", ".yml",
        ".sql", ".sh", ".bat", ".ps1", ".ini", ".cfg", ".toml",
    ]
)

TOOL_EXTENSIONS = frozenset(
    os.getenv("TOOL_EXTENSIONS", "").split(",") if os.getenv("TOOL_EXTENSIONS")
    else [
        ".docx", ".doc",
        ".xlsx", ".xls",
        ".pptx", ".ppt",
        ".zip", ".tar", ".rar", ".7z",
        ".png", ".jpg", ".jpeg", ".bmp", ".webp",
        ".pdf",
    ]
)

# ── OCR ───────────────────────────────────────────────────────────────

OCR_LANGUAGES = os.getenv("OCR_LANGUAGES", "ch_sim,en").split(",")
OCR_GPU = os.getenv("OCR_GPU", "false").lower() == "true"

# ── LibreOffice ───────────────────────────────────────────────────────

def _find_soffice() -> str:
    import shutil
    env = os.getenv("SOFFICE_PATH", "")
    if env and Path(env).exists():
        return env
    for p in (
        Path("C:/Program Files/LibreOffice/program/soffice.exe"),
        Path("/usr/bin/soffice"),
    ):
        if p.exists():
            return str(p)
    # scoop (Windows)
    scoop_base = Path.home() / "scoop" / "apps" / "libreoffice"
    if scoop_base.exists():
        try:
            for d in sorted(scoop_base.iterdir(), reverse=True):
                p = d / "LibreOffice" / "program" / "soffice.exe"
                if p.exists():
                    return str(p)
        except Exception:
            pass
    found = shutil.which("soffice") or shutil.which("soffice.exe") or ""
    return found

SOFFICE_PATH = _find_soffice()

# ── Pandoc ────────────────────────────────────────────────────────────

def _find_pandoc() -> str:
    import shutil
    return shutil.which("pandoc") or shutil.which("pandoc.exe") or ""

PANDOC_PATH = _find_pandoc()
