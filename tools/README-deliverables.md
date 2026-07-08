# 交付物生成说明

生成课程验收交付物：

```bash
ai-service/.venv/bin/python tools/generate_deliverables.py
```

输出目录：

```text
deliverables/submission-2026-06-29/
```

脚本会生成：

- 5 份 `.docx` 文档
- 1 份 `.pptx` 演示文稿
- 1 个安装包 `.zip`
- 1 个源码包 `.zip`
- 1 份交付说明 `00-交付说明.md`

生成前提：

- 后端 `target/Teaching-System-1.0-SNAPSHOT.jar` 已存在
- 前端 `frontend/dist/` 已存在
- `ai-service/.venv` 中已安装 `python-docx`、`python-pptx`、`Pillow`
