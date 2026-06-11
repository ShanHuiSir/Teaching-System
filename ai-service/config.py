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
CORS_ORIGINS = os.getenv("CORS_ORIGINS", "http://localhost:8080,http://127.0.0.1:8080,https://redtree.miprota.cc").split(",")
SERVER_URL = os.getenv("AI_SERVICE_SERVER_URL", "http://localhost:8000")

# ── DeepSeek / LLM ────────────────────────────────────────────────────

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_BASE_URL = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")

# Model: deepseek-v4-pro (premium, 1.6T MoE, 49B active) or
#        deepseek-v4-flash (fast, 284B MoE, 13B active)
# Legacy deepseek-chat / deepseek-reasoner will be retired 2026-07-24
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-v4-flash")

# Thinking mode: when enabled, the model produces an internal reasoning
# trace before answering. Only supported on v4-pro (v4-flash ignores it).
DEEPSEEK_THINKING = os.getenv("DEEPSEEK_THINKING", "false").lower() == "true"

# Reasoning depth: "high" (default, balanced) or "max" (best quality,
# burns more tokens). low/medium → high; xhigh → max.
DEEPSEEK_REASONING_EFFORT = os.getenv("DEEPSEEK_REASONING_EFFORT", "high")

DEEPSEEK_TEMPERATURE = float(os.getenv("DEEPSEEK_TEMPERATURE", "0.3"))
DEEPSEEK_MAX_TOKENS = int(os.getenv("DEEPSEEK_MAX_TOKENS", "4096"))

# ── Retry ─────────────────────────────────────────────────────────────

RETRY_MAX_ATTEMPTS = int(os.getenv("RETRY_MAX_ATTEMPTS", "3"))
RETRY_BASE_DELAY_SECONDS = float(os.getenv("RETRY_BASE_DELAY_SECONDS", "1.0"))
RETRY_MAX_DELAY_SECONDS = float(os.getenv("RETRY_MAX_DELAY_SECONDS", "10.0"))

# ── Rate limiting ─────────────────────────────────────────────────────

AI_RATE_LIMIT_PER_MINUTE = int(os.getenv("AI_RATE_LIMIT_PER_MINUTE", "10"))

# ── Evaluation logging ─────────────────────────────────────────────────

EVAL_LOG_DIR = os.getenv("EVAL_LOG_DIR", str(Path(__file__).resolve().parent / "logs"))
EVAL_LOG_FILE = os.getenv("EVAL_LOG_FILE", "eval_results.jsonl")
EVAL_CONTENT_MAX_CHARS = int(os.getenv("EVAL_CONTENT_MAX_CHARS", "32000"))

# ── Evaluation prompt ──────────────────────────────────────────────────

EVAL_SYSTEM_PROMPT_TEMPLATE = os.getenv("EVAL_SYSTEM_PROMPT_TEMPLATE", """\
你是一位严格但公正的教学评价专家。根据学生提交的作业内容进行评分。

评分规则：
- 满分 100 分，从 60 分基准开始
- 按下方指定的评分维度分别打分，aiScore 为各维度分数的加权总和

{subject_context}

按以下 JSON 格式输出，不要输出其他内容：
{
  "aiScore": 85,
  "aiIssues": "1. 问题一\\n2. 问题二\\n3. 问题三",
  "aiComment": "一段 50-150 字的综合评价，语气鼓励性但实事求是。",
  "dimensionScores": [
    {{"name": "维度名称", "score": 88, "comment": "该维度的简短评语，10-30字"}}
  ]
}

注意：
- aiScore 为 0-100 的数值，等于 dimensionScores 中各维度分数按权重加权求和
- aiIssues 列出具体的问题，至少 2 条，最多 5 条，每条以 "N. " 开头，换行分隔
- aiComment 为一段 50-150 字的综合评价
- dimensionScores 为每个评分维度的得分和简短评语，维度名称必须与给定的完全一致\
""")

# ── Legacy prompt (kept for backward compat) ───────────────────────────

EVAL_SYSTEM_PROMPT = EVAL_SYSTEM_PROMPT_TEMPLATE.replace("{subject_context}", "")

# ── Subject-aware contexts ─────────────────────────────────────────────

SUBJECT_CONTEXTS = {
    "code": """\
学科背景：这是一份编程作业。评估时请重点关注：
- 代码结构是否清晰，命名是否规范
- 功能是否完整实现，逻辑是否正确
- 是否有适当的注释和文档说明
- 是否考虑了边界情况和错误处理
- 代码风格是否统一，是否遵循常见最佳实践""",

    "document": """\
学科背景：这是一份文档/报告类作业。评估时请重点关注：
- 文档结构是否完整，逻辑层次是否分明
- 内容是否翔实准确，论点是否有据可依
- 语言表达是否清晰流畅，格式是否规范
- 是否包含必要的图表、参考文献等辅助内容
- 结论是否与内容一致，是否有独立见解""",

    "design": """\
学科背景：这是一份设计类作业。评估时请重点关注：
- 设计方案是否合理，是否满足需求规格
- 设计思路是否清晰，技术选型是否有依据
- 架构图/流程图/UML 等设计文档是否规范
- 是否考虑了可扩展性、性能、安全性等非功能需求
- 设计创新性和可行性是否平衡""",

    "general": """\
学科背景：这是一份综合性作业。评估时请重点关注：
- 整体完成度和内容完整性
- 逻辑清晰度和结构合理性
- 格式规范性和语言表达能力
- 创新思维和独立思考的体现
- 细节处理和整体质量""",
}

# ── Default rubrics by subject type ────────────────────────────────────

DEFAULT_RUBRICS = {
    "code": [
        {"name": "代码质量", "weight": 0.35, "description": "代码结构清晰、命名规范、注释完整、风格统一"},
        {"name": "功能完整性", "weight": 0.35, "description": "需求功能全部实现，逻辑正确，边界情况已处理"},
        {"name": "文档与说明", "weight": 0.15, "description": "README、API 文档、关键逻辑注释充分"},
        {"name": "创新与优化", "weight": 0.15, "description": "方案设计有独到之处，性能或可维护性有优化"},
    ],
    "document": [
        {"name": "内容完整性", "weight": 0.35, "description": "覆盖所有要求的知识点，信息充实准确"},
        {"name": "逻辑与结构", "weight": 0.30, "description": "段落层次分明，逻辑递进合理，过渡自然"},
        {"name": "格式规范", "weight": 0.20, "description": "排版整齐，图表清晰，引用格式正确"},
        {"name": "表达与创新", "weight": 0.15, "description": "语言流畅，有独立分析和创新观点"},
    ],
    "design": [
        {"name": "方案合理性", "weight": 0.35, "description": "设计方案满足需求，技术选型有依据，架构合理"},
        {"name": "文档规范性", "weight": 0.30, "description": "设计图/流程图/UML 规范清晰，说明充分"},
        {"name": "技术深度", "weight": 0.20, "description": "对技术原理有深入理解，方案细节完整"},
        {"name": "创新与可行", "weight": 0.15, "description": "方案有创新点，同时具备实现可行性"},
    ],
    "general": [
        {"name": "完成度", "weight": 0.40, "description": "作业内容的完整性和覆盖度"},
        {"name": "正确性", "weight": 0.30, "description": "内容准确无误，逻辑正确"},
        {"name": "规范性", "weight": 0.30, "description": "格式、排版、语言表达的质量"},
    ],
}

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

OCR_ENABLED = os.getenv("OCR_ENABLED", "false").lower() == "true"
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
