FROM python:3.12-slim

WORKDIR /srv

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1

# Install dependencies (app sources copied first so the wheel can build).
COPY pyproject.toml ./
COPY app ./app
RUN pip install --upgrade pip && pip install .

EXPOSE 8000

# Render provides $PORT; default to 8000 locally.
CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT:-8000}"]
