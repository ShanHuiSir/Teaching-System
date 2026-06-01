"""
AI 评价服务 — 独立 Python HTTP 服务
Java 端通过 POST /api/evaluate 调用，契约对齐 AIService.evaluate(AIEvalRequest)
"""

from flask import Flask, request, jsonify

app = Flask(__name__)


@app.route("/api/evaluate", methods=["POST"])
def evaluate():
    body = request.get_json()
    if body is None:
        return jsonify({"message": "请求体格式错误"}), 400

    student_name = body.get("studentName", "")
    file_name = body.get("fileName", "")

    if not student_name:
        return jsonify({"message": "学生姓名不能为空"}), 400
    if not file_name:
        return jsonify({"message": "文件名不能为空"}), 400

    # TODO: 替换为真实 LLM 评价逻辑
    return jsonify({
        "aiScore": 82.50,
        "aiIssues": (
            "1. 结构不够清晰，建议优化段落层次\n"
            "2. 缺少核心论点支撑材料\n"
            "3. 格式规范性不足，标题层级需统一"
        ),
        "aiComment": "整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。",
        "status": 1,
    })


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)
