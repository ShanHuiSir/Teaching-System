"""AI evaluator — DeepSeek-powered scoring via OpenAI-compatible API."""

from __future__ import annotations

import json
from typing import Optional

from openai import OpenAI

import config

_client: Optional[OpenAI] = None


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


def evaluate(text: str, student_name: str = "") -> dict:
    """Call DeepSeek API to score the extracted text content."""
    if not text.strip():
        return {
            "aiScore": 0,
            "aiIssues": "1. 作业内容为空，请重新提交",
            "aiComment": "未检测到有效内容，请确认文件是否损坏或格式是否正确。",
        }

    client = _get_client()
    user_content = (
        f"学生姓名：{student_name}\n\n作业内容：\n{text}"
        if student_name
        else f"作业内容：\n{text}"
    )

    try:
        response = client.chat.completions.create(
            model=config.DEEPSEEK_MODEL,
            messages=[
                {"role": "system", "content": config.EVAL_SYSTEM_PROMPT},
                {"role": "user", "content": user_content},
            ],
            response_format={"type": "json_object"},
            temperature=config.DEEPSEEK_TEMPERATURE,
            max_tokens=config.DEEPSEEK_MAX_TOKENS,
        )
    except Exception as e:
        raise RuntimeError(f"DeepSeek API call failed: {e}")

    raw = response.choices[0].message.content or "{}"

    try:
        result = json.loads(raw)
    except json.JSONDecodeError:
        return {
            "aiScore": 0,
            "aiIssues": "1. AI 返回格式异常，请联系教师人工评阅",
            "aiComment": f"AI 返回了非 JSON 内容，原始输出：{raw[:200]}",
        }

    return {
        "aiScore": float(result.get("aiScore", 0)),
        "aiIssues": str(result.get("aiIssues", "")),
        "aiComment": str(result.get("aiComment", "")),
    }
