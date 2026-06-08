"""AI evaluator — DeepSeek v4 scoring via OpenAI-compatible API.

Supports:
- deepseek-v4-pro (1.6T MoE, 49B active, with optional thinking mode)
- deepseek-v4-flash (284B MoE, 13B active, fast/cheap)
- thinking mode + reasoning_effort control for deeper analysis
- rubric-based multi-dimension scoring
- subject-type-aware system prompts
- exponential-backoff retry on transient failures
"""

from __future__ import annotations

import json
import logging
import time
from typing import Optional

from openai import (
    APIConnectionError,
    InternalServerError,
    RateLimitError,
    APITimeoutError,
    OpenAI,
)

import config

logger = logging.getLogger(__name__)

_client: Optional[OpenAI] = None

# Exceptions that warrant a retry (transient / server-side)
_TRANSIENT_EXCEPTIONS = (
    APITimeoutError,
    APIConnectionError,
    InternalServerError,
    RateLimitError,
)


def _get_client() -> OpenAI:
    global _client
    if _client is None:
        if not config.DEEPSEEK_API_KEY:
            raise RuntimeError("DEEPSEEK_API_KEY not set")
        _client = OpenAI(
            api_key=config.DEEPSEEK_API_KEY,
            base_url=config.DEEPSEEK_BASE_URL,
        )
    return _client


# ── rubric validation ─────────────────────────────────────────────────────

def _validate_rubric(dimensions: list[dict]) -> None:
    """Raise ValueError if the rubric dimensions are invalid."""
    if not dimensions:
        raise ValueError("Rubric dimensions list is empty")
    if len(dimensions) > 8:
        raise ValueError("Rubric must have 1–8 dimensions")
    total_weight = 0.0
    seen_names: set[str] = set()
    for d in dimensions:
        name = d.get("name", "")
        if not name:
            raise ValueError("Each rubric dimension must have a 'name'")
        if name in seen_names:
            raise ValueError(f"Duplicate dimension name: {name}")
        seen_names.add(name)
        weight = d.get("weight", 0)
        if not isinstance(weight, (int, float)) or weight < 0 or weight > 1:
            raise ValueError(f"Dimension '{name}' weight must be 0–1, got {weight}")
        total_weight += weight
    if abs(total_weight - 1.0) > 0.01:
        raise ValueError(
            f"Rubric weights sum to {total_weight:.3f}, must be 1.0 (±0.01)"
        )


def _resolve_rubric(rubric_json: dict | None, subject_type: str) -> list[dict]:
    """Return the dimensions list to use.

    If rubric_json is provided and valid, use it.
    Otherwise fall back to the default rubric for subject_type.
    Unknown subject_type falls back to "general".
    """
    if rubric_json is not None:
        dims = rubric_json.get("dimensions", [])
        _validate_rubric(dims)
        return dims

    safe_type = subject_type if subject_type in config.DEFAULT_RUBRICS else "general"
    if safe_type != subject_type:
        logger.warning("Unknown subject_type '%s', falling back to 'general'", subject_type)
    return config.DEFAULT_RUBRICS[safe_type]


# ── prompt building ───────────────────────────────────────────────────────

def build_system_prompt(subject_type: str, rubric_dimensions: list[dict]) -> str:
    """Build the full system prompt with subject context and rubric dimensions."""
    ctx = config.SUBJECT_CONTEXTS.get(subject_type, config.SUBJECT_CONTEXTS["general"])
    prompt = config.EVAL_SYSTEM_PROMPT_TEMPLATE.replace("{subject_context}", ctx)

    dim_lines = ["\n请严格按以下维度分别评分："]
    for d in rubric_dimensions:
        name = d["name"]
        weight_pct = round(d["weight"] * 100)
        desc = d.get("description", "")
        if desc:
            dim_lines.append(f"- {name}（权重 {weight_pct}%）：{desc}")
        else:
            dim_lines.append(f"- {name}（权重 {weight_pct}%）")
    dim_lines.append(
        "\n每个维度在 dimensionScores 中给出 score（0-100）和 comment（10-30字评语）。"
        "aiScore 必须是各维度 score 乘以对应权重后求和的结果。"
    )
    prompt += "\n".join(dim_lines)
    return prompt


# ── retry logic ───────────────────────────────────────────────────────────

def _call_deepseek_with_retry(
    messages: list[dict],
    extra_body: dict | None,
) -> tuple[object, int]:
    """Call DeepSeek API with exponential-backoff retry.

    Returns (response, attempt_count).
    Only retries on transient errors (timeout, connection, 5xx, rate-limit).
    """
    last_exc: Exception | None = None
    for attempt in range(config.RETRY_MAX_ATTEMPTS):
        try:
            client = _get_client()
            response = client.chat.completions.create(
                model=config.DEEPSEEK_MODEL,
                messages=messages,
                response_format={"type": "json_object"},
                temperature=config.DEEPSEEK_TEMPERATURE,
                max_tokens=config.DEEPSEEK_MAX_TOKENS,
                extra_body=extra_body if extra_body else None,
            )
            return response, attempt + 1
        except _TRANSIENT_EXCEPTIONS as e:
            last_exc = e
            delay = min(
                config.RETRY_BASE_DELAY_SECONDS * (2**attempt),
                config.RETRY_MAX_DELAY_SECONDS,
            )
            logger.warning(
                "DeepSeek API attempt %d/%d failed: %s. Retrying in %.1fs",
                attempt + 1,
                config.RETRY_MAX_ATTEMPTS,
                e,
                delay,
            )
            time.sleep(delay)
        except Exception as e:
            raise RuntimeError(f"DeepSeek API non-transient error: {e}") from e

    raise RuntimeError(
        f"DeepSeek API failed after {config.RETRY_MAX_ATTEMPTS} attempts: {last_exc}"
    )


# ── SSE helpers ────────────────────────────────────────────────────────────

def _format_sse(event: str, data: dict | str) -> str:
    """Format a single SSE event frame."""
    payload = json.dumps(data, ensure_ascii=False) if isinstance(data, dict) else data
    return f"event: {event}\ndata: {payload}\n\n"


# ── evaluation ────────────────────────────────────────────────────────────

def evaluate(
    text: str,
    student_name: str = "",
    rubric_json: dict | None = None,
    subject_type: str = "general",
) -> dict:
    """Call DeepSeek v4 API to score the extracted text content.

    Args:
        text: The extracted plain-text content of the submission.
        student_name: Student display name.
        rubric_json: Optional dict with "dimensions" list. If None, the
                     default rubric for subject_type is used.
        subject_type: One of "code", "document", "design", "general".

    Returns:
        dict with aiScore, aiIssues, aiComment, dimensionScores.
    """
    if not text.strip():
        return {
            "aiScore": 0,
            "aiIssues": "1. 作业内容为空，请重新提交",
            "aiComment": "未检测到有效内容，请确认文件是否损坏或格式是否正确。",
            "dimensionScores": [],
        }

    # Validate and resolve rubric dimensions
    try:
        dimensions = _resolve_rubric(rubric_json, subject_type)
    except ValueError as e:
        raise ValueError(f"Invalid rubric: {e}") from e

    # Build system prompt
    system_prompt = build_system_prompt(subject_type, dimensions)

    # Truncate content if too long
    content = text
    if len(content) > config.EVAL_CONTENT_MAX_CHARS:
        logger.warning(
            "Content truncated from %d to %d chars for evaluation",
            len(text),
            config.EVAL_CONTENT_MAX_CHARS,
        )
        content = content[: config.EVAL_CONTENT_MAX_CHARS]

    user_content = (
        f"学生姓名：{student_name}\n\n作业内容：\n{content}"
        if student_name
        else f"作业内容：\n{content}"
    )

    extra_body: dict = {}
    if config.DEEPSEEK_THINKING:
        extra_body["thinking"] = {"type": "enabled"}
        extra_body["reasoning_effort"] = config.DEEPSEEK_REASONING_EFFORT

    t0 = time.perf_counter()
    success = False
    attempt_count = 0
    error_msg: str | None = None
    eval_result: dict | None = None

    try:
        response, attempt_count = _call_deepseek_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_content},
            ],
            extra_body=extra_body,
        )
        raw = response.choices[0].message.content or "{}"
        result = json.loads(raw)
        success = True
    except RuntimeError as e:
        error_msg = str(e)
        raise
    except json.JSONDecodeError:
        error_msg = f"AI returned non-JSON content: {raw[:200] if 'raw' in dir() else 'N/A'}"
        logger.warning(error_msg)
        eval_result = {
            "aiScore": 0,
            "aiIssues": "1. AI 返回格式异常，请联系教师人工评阅",
            "aiComment": f"AI 返回了非 JSON 内容，原始输出：{raw[:200] if 'raw' in dir() else 'N/A'}",
            "dimensionScores": [],
        }
        success = False
    finally:
        latency_ms = (time.perf_counter() - t0) * 1000
        if success and 'result' in dir():
            eval_result = _build_result(result, dimensions)

        from evaluator.logger import log_evaluation

        log_evaluation(
            student_name=student_name,
            subject_type=subject_type,
            rubric_used=dimensions,
            eval_result=eval_result,
            latency_ms=latency_ms,
            attempt_count=attempt_count,
            success=success,
            error=error_msg,
        )

    return eval_result


def evaluate_stream(
    text: str,
    student_name: str = "",
    rubric_json: dict | None = None,
    subject_type: str = "general",
):
    """Stream DeepSeek v4 scoring as SSE events.

    Yields SSE-formatted strings (event + data). Same scoring logic as
    evaluate(), but delivers reasoning and content tokens in real-time.

    Events: start, reasoning, content, result, error, done.
    """
    # ── empty content ──────────────────────────────────────────────────
    if not text.strip():
        yield _format_sse("result", {
            "aiScore": 0,
            "aiIssues": "1. 作业内容为空，请重新提交",
            "aiComment": "未检测到有效内容，请确认文件是否损坏或格式是否正确。",
            "dimensionScores": [],
        })
        yield _format_sse("done", {})
        return

    # ── pre-flight (same as evaluate) ───────────────────────────────────
    try:
        dimensions = _resolve_rubric(rubric_json, subject_type)
    except ValueError as e:
        yield _format_sse("error", {"message": f"Invalid rubric: {e}", "code": "INVALID_RUBRIC"})
        yield _format_sse("done", {})
        return

    system_prompt = build_system_prompt(subject_type, dimensions)

    content = text
    if len(content) > config.EVAL_CONTENT_MAX_CHARS:
        logger.warning(
            "Content truncated from %d to %d chars for evaluation",
            len(text), config.EVAL_CONTENT_MAX_CHARS,
        )
        content = content[: config.EVAL_CONTENT_MAX_CHARS]

    user_content = (
        f"学生姓名：{student_name}\n\n作业内容：\n{content}"
        if student_name
        else f"作业内容：\n{content}"
    )

    extra_body: dict = {}
    if config.DEEPSEEK_THINKING:
        extra_body["thinking"] = {"type": "enabled"}
        extra_body["reasoning_effort"] = config.DEEPSEEK_REASONING_EFFORT

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_content},
    ]

    yield _format_sse("start", {
        "subjectType": subject_type,
        "dimensions": [{"name": d["name"], "weight": d["weight"]} for d in dimensions],
    })

    # ── retry loop (connection only, not mid-stream) ────────────────────
    t0 = time.perf_counter()
    attempt_count = 0
    last_exc: Exception | None = None
    stream = None

    for attempt in range(config.RETRY_MAX_ATTEMPTS):
        try:
            client = _get_client()
            stream = client.chat.completions.create(
                model=config.DEEPSEEK_MODEL,
                messages=messages,
                response_format={"type": "json_object"},
                temperature=config.DEEPSEEK_TEMPERATURE,
                max_tokens=config.DEEPSEEK_MAX_TOKENS,
                extra_body=extra_body if extra_body else None,
                stream=True,
                stream_options={"include_usage": True},
            )
            attempt_count = attempt + 1
            break
        except _TRANSIENT_EXCEPTIONS as e:
            last_exc = e
            delay = min(
                config.RETRY_BASE_DELAY_SECONDS * (2**attempt),
                config.RETRY_MAX_DELAY_SECONDS,
            )
            logger.warning(
                "DeepSeek stream attempt %d/%d failed: %s. Retrying in %.1fs",
                attempt + 1, config.RETRY_MAX_ATTEMPTS, e, delay,
            )
            time.sleep(delay)
        except Exception as e:
            yield _format_sse("error", {"message": f"DeepSeek API error: {e}", "code": "DEEPSEEK_API_FAILURE"})
            yield _format_sse("done", {})
            return

    if stream is None:
        yield _format_sse("error", {
            "message": f"DeepSeek API failed after {config.RETRY_MAX_ATTEMPTS} attempts: {last_exc}",
            "code": "DEEPSEEK_API_FAILURE",
        })
        yield _format_sse("done", {})
        return

    # ── iterate stream chunks ───────────────────────────────────────────
    content_buffer: list[str] = []
    success = False
    error_msg: str | None = None
    eval_result: dict | None = None

    try:
        for chunk in stream:
            if chunk.usage:
                continue
            if not chunk.choices:
                continue

            delta = chunk.choices[0].delta
            if getattr(delta, "reasoning_content", None):
                yield _format_sse("reasoning", {"delta": delta.reasoning_content})
            if delta.content:
                content_buffer.append(delta.content)
                yield _format_sse("content", {"delta": delta.content})
    except Exception as e:
        error_msg = f"Stream interrupted: {e}"
        logger.warning(error_msg)
        yield _format_sse("error", {"message": error_msg, "code": "STREAM_INTERRUPTED"})
    else:
        # ── parse accumulated content ───────────────────────────────────
        raw_text = "".join(content_buffer)
        try:
            raw = json.loads(raw_text)
            eval_result = _build_result(raw, dimensions)
            success = True
            yield _format_sse("result", eval_result)
        except json.JSONDecodeError:
            error_msg = f"AI returned non-JSON content in stream: {raw_text[:200]}"
            logger.warning(error_msg)
            eval_result = {
                "aiScore": 0,
                "aiIssues": "1. AI 返回格式异常，请联系教师人工评阅",
                "aiComment": f"AI 返回了非 JSON 内容，原始输出：{raw_text[:200]}",
                "dimensionScores": [],
            }
            yield _format_sse("result", eval_result)
    finally:
        latency_ms = (time.perf_counter() - t0) * 1000
        from evaluator.logger import log_evaluation

        log_evaluation(
            student_name=student_name,
            subject_type=subject_type,
            rubric_used=dimensions,
            eval_result=eval_result if success else None,
            latency_ms=latency_ms,
            attempt_count=attempt_count,
            success=success,
            error=error_msg,
        )
        yield _format_sse("done", {})


def _build_result(raw: dict, dimensions: list[dict]) -> dict:
    """Parse the API response and build the standardized result dict."""
    dim_scores_raw = raw.get("dimensionScores", [])

    # Cross-check: compute weighted total from dimension scores
    weighted_total = 0.0
    dim_name_to_weight = {d["name"]: d["weight"] for d in dimensions}
    validated_scores = []
    for ds in dim_scores_raw:
        name = ds.get("name", "")
        score = float(ds.get("score", 0))
        comment = str(ds.get("comment", ""))
        if name in dim_name_to_weight:
            validated_scores.append(
                {"name": name, "score": score, "comment": comment}
            )
            weighted_total += score * dim_name_to_weight[name]
        else:
            logger.warning("AI returned score for unknown dimension '%s', ignored", name)

    if validated_scores:
        if abs(weighted_total - float(raw.get("aiScore", 0))) > 5:
            logger.warning(
                "Weighted total (%.1f) differs from aiScore (%s) by >5 points",
                weighted_total,
                raw.get("aiScore"),
            )
    elif dim_scores_raw:
        validated_scores = [
            {"name": ds.get("name", "unknown"), "score": float(ds.get("score", 0)),
             "comment": str(ds.get("comment", ""))}
            for ds in dim_scores_raw
        ]

    return {
        "aiScore": float(raw.get("aiScore", 0)),
        "aiIssues": str(raw.get("aiIssues", "")),
        "aiComment": str(raw.get("aiComment", "")),
        "dimensionScores": validated_scores,
    }
