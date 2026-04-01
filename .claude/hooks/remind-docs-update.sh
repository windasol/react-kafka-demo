#!/bin/bash
# PostToolUse hook: 소스 파일 수정 후 docs 업데이트 리마인드
#
# 동작 방식:
#   Edit / Write 로 소스 파일이 변경되면
#   해당 서비스의 service doc + codebase-map.md 업데이트 필요 여부를 알림

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null)

PROJECT_ROOT="/home/user/react-kafka-demo"

# .claude/ 내 파일(docs 자체)은 무시
if echo "$FILE_PATH" | grep -q "${PROJECT_ROOT}/.claude/"; then
    exit 0
fi

# 소스 파일(.java, .ts, .tsx)만 검사
if ! echo "$FILE_PATH" | grep -qE '\.(java|tsx?)$'; then
    exit 0
fi

# 서비스별 docs 매핑
if echo "$FILE_PATH" | grep -q "auth-service"; then
    DOC=".claude/docs/services/auth.md"
elif echo "$FILE_PATH" | grep -q "order-service"; then
    DOC=".claude/docs/services/order.md"
elif echo "$FILE_PATH" | grep -q "notification-service"; then
    DOC=".claude/docs/services/notification.md"
elif echo "$FILE_PATH" | grep -q "jwt-common"; then
    DOC=".claude/docs/services/jwt-common.md"
elif echo "$FILE_PATH" | grep -q "frontend/src"; then
    DOC=".claude/docs/services/frontend.md"
else
    exit 0
fi

BASENAME=$(basename "$FILE_PATH")

echo "[docs-sync] ${BASENAME} 수정됨 → 시그니처·필드·엔드포인트 변경이 있으면 커밋 전에 반드시 업데이트:"
echo "  1. ${DOC}"
echo "  2. .claude/docs/codebase-map.md"

exit 0
