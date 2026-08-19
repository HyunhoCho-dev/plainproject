#!/usr/bin/env bash

# Codespace가 열릴 때 PLAIN의 AI 서버와 Spring 백엔드를 함께 실행합니다.
# API 키는 파일에서 읽지 않고 GitHub Codespaces Secret 환경변수에서만 읽습니다.
set -u

WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="/tmp/plainproject"
mkdir -p "$LOG_DIR"

if [[ -z "${OPENROUTER_API_KEY:-}" ]]; then
  echo "[PLAIN] OPENROUTER_API_KEY Codespaces Secret이 없습니다."
  echo "[PLAIN] 저장소 관리자에게 Codespaces Secret 등록을 요청하세요."
  exit 1
fi

# 이미 실행 중이면 중복으로 서버를 띄우지 않습니다.
if ! curl --silent --fail http://localhost:3000/health >/dev/null 2>&1; then
  echo "[PLAIN] AI 서버를 시작합니다. 로그: $LOG_DIR/ai.log"
  (
    cd "$WORKSPACE_DIR/ai"
    nohup npm start >"$LOG_DIR/ai.log" 2>&1 &
  )
fi

if ! curl --silent --fail http://localhost:8080/ >/dev/null 2>&1; then
  echo "[PLAIN] 백엔드와 프론트엔드를 시작합니다. 로그: $LOG_DIR/backend.log"
  (
    cd "$WORKSPACE_DIR/backend"
    nohup ./gradlew bootRun --args='--plain.ai.timeout-seconds=180' >"$LOG_DIR/backend.log" 2>&1 &
  )
fi

# 서버 준비가 끝났는지 확인해 접속 가능한 주소만 안내합니다.
for _ in $(seq 1 180); do
  if curl --silent --fail http://localhost:3000/health >/dev/null 2>&1 \
    && curl --silent --fail http://localhost:8080/prototype/index.html >/dev/null 2>&1; then
    echo "[PLAIN] 실행 완료: 포트 8080의 'PLAIN 서비스'를 여세요."
    exit 0
  fi
  sleep 1
done

echo "[PLAIN] 서버가 제한 시간 안에 준비되지 않았습니다."
echo "[PLAIN] AI 로그: $LOG_DIR/ai.log"
echo "[PLAIN] 백엔드 로그: $LOG_DIR/backend.log"
exit 1
