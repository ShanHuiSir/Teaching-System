"""AI evaluator — DeepSeek-powered scoring via OpenAI-compatible API."""

from __future__ import annotations

import json
import os

from openai import OpenAI

_client: OpenAI | None = None


def _get_client() -> OpenAI:
    global _client
    if _client is None:
        api_key = os.getenv("DEEPSEEK_API_KEY", "")
        if not api_key:
            raise RuntimeError("DEEPSEEK_API_KEY not set")
        _client = OpenAI(api_key=api_key, base_url="https://api.deepseek.com")
    return _client


SYSTEM_PROMPT = """你是一位严格但公正的教学评价专家。根据学生提交的作业内容进行评分。

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
- aiComment 为一段 50-150 字的综合评价"""


def evaluate(text: str, student_name: str = "") -> dict:
    """Call DeepSeek API to score the extracted text content.

    Args:
        text: Extracted plain text from the student's submission.
        student_name: Student name for context.

    Returns:
        {"aiScore": float, "aiIssues": str, "aiComment": str}
    """
    if not text.strip():
        return {
            "aiScore": 0,
            "aiIssues": "1. 作业内容为空，请重新提交",
            "aiComment": "未检测到有效内容，请确认文件是否损坏或格式是否正确。",
        }

    client = _get_client()
    user_content = f"学生姓名：{student_name}\n\n作业内容：\n{text}" if student_name else f"作业内容：\n{text}"

    try:
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": user_content},
            ],
            response_format={"type": "json_object"},
            temperature=0.3,
            max_tokens=800,
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
