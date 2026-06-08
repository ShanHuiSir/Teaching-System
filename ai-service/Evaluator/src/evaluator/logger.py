"""Structured JSON-lines logging for evaluation results.

Writes one JSON object per line to the configured log file.
Rotation is handled externally (logrotate or similar).
"""

from __future__ import annotations

import json
import logging
import os
from datetime import datetime, timezone
from pathlib import Path

import config

logger = logging.getLogger(__name__)


def log_evaluation(
    *,
    student_name: str,
    subject_type: str,
    rubric_used: list[dict],
    eval_result: dict | None,
    latency_ms: float,
    attempt_count: int,
    success: bool,
    error: str | None = None,
) -> None:
    """Append a single JSON line to the evaluation log file."""
    try:
        log_dir = Path(config.EVAL_LOG_DIR)
        log_dir.mkdir(parents=True, exist_ok=True)
        log_path = log_dir / config.EVAL_LOG_FILE

        entry = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "student_name": student_name,
            "subject_type": subject_type,
            "rubric": rubric_used,
            "result": eval_result if success else None,
            "latency_ms": round(latency_ms, 2),
            "attempts": attempt_count,
            "success": success,
            "error": error,
        }
        with open(log_path, "a", encoding="utf-8") as f:
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    except Exception:
        logger.warning("Failed to write evaluation log entry", exc_info=True)
