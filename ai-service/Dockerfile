FROM docker.1ms.run/library/python:3.11-slim-bookworm

WORKDIR /app

ARG PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_INDEX_URL=${PIP_INDEX_URL} \
    PIP_TRUSTED_HOST=pypi.tuna.tsinghua.edu.cn \
    AI_SERVICE_HOST=0.0.0.0 \
    AI_SERVICE_PORT=8000 \
    AI_SERVICE_SERVER_URL=http://localhost:8000 \
    OCR_ENABLED=false \
    EVAL_LOG_DIR=/app/logs

COPY requirements-docker.txt config.py ./
COPY DocxConv ./DocxConv
COPY Evaluator ./Evaluator
COPY ScreenshotProc ./ScreenshotProc
COPY ArchiveProc ./ArchiveProc

RUN pip install --retries 5 --timeout 120 -r requirements-docker.txt \
    && pip install --no-deps -e DocxConv -e Evaluator -e ArchiveProc \
    && mkdir -p /app/logs

EXPOSE 8000
VOLUME ["/app/logs"]

CMD ["python", "-m", "docxconv", "serve", "--host", "0.0.0.0", "--port", "8000"]
