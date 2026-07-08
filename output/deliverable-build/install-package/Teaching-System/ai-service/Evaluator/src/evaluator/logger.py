"""Structured JSON-lines logging for evaluation results.

Writes one JSON object per line to the configured log file.
Rotation is handled externally (logrotate or similar).

Two log functions:
- log_evaluation       — AI-only entry (legacy, kept for backward compat)
- log_final_evaluation — merged AI + teacher entry, written after teacher review
"""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path

import config

logger = logging.getLogger(__name__)


def _write_entry(entry: dict) -> None:
    """Append a single JSON line to the evaluation log file."""
    try:
        log_dir = Path(config.EVAL_LOG_DIR)
        log_dir.mkdir(parents=True, exist_ok=True)
        log_path = log_dir / config.EVAL_LOG_FILE
        with open(log_path, "a", encoding="utf-8") as f:
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    except Exception:
        logger.warning("Failed to write evaluation log entry", exc_info=True)


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
    """Append an AI-only evaluation log entry (legacy).

    Kept for backward compatibility; new code should use log_final_evaluation
    to capture both AI and teacher evaluation in a single entry.
    """
    _write_entry({
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "student_name": student_name,
        "subject_type": subject_type,
        "rubric": rubric_used,
        "result": eval_result if success else None,
        "latency_ms": round(latency_ms, 2),
        "attempts": attempt_count,
        "success": success,
        "error": error,
    })


def log_final_evaluation(
    *,
    student_name: str,
    ai_score: float,
    ai_issues: str,
    ai_comment: str,
    dimension_scores: list[dict],
    teacher_score: float,
    teacher_comment: str,
) -> None:
    """Append a final evaluation log entry after teacher review completes.

    Called from the /api/eval-log endpoint, which is triggered by the Java
    backend after EvaluationService.saveTeacherReview() succeeds.
    """
    _write_entry({
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "student_name": student_name,
        "ai_score": ai_score,
        "ai_issues": ai_issues,
        "ai_comment": ai_comment,
        "dimension_scores": dimension_scores,
        "teacher_score": teacher_score,
        "teacher_comment": teacher_comment,
    })
