#!/bin/bash
# PreToolUse hook: 소스 파일 Read 전 codebase-map.md 확인 강제
#
# 동작 방식:
#   1. .java / .ts / .tsx / .css 파일을 Read하려 할 때 검사
#   2. codebase-map.md 를 이미 읽은 세션이면 허용
#   3. 읽지 않았으면 차단 → Claude가 먼저 codebase-map.md 를 읽도록 유도
#   4. 상태는 /tmp 에 4시간 유지 (세션 재시작 시 자동 만료)

INPUT=$(cat)

FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null)

PROJECT_ROOT="/home/user/react-kafka-demo"
PROJECT_HASH=$(echo "$PROJECT_ROOT" | cksum | cut -d' ' -f1)
STATE_FILE="/tmp/.codemap_read_${PROJECT_HASH}"

# codebase-map.md 읽는 경우 → 상태 마킹 후 허용
if echo "$FILE_PATH" | grep -q "codebase-map\.md"; then
    touch "$STATE_FILE"
    exit 0
fi

# .claude/ 내 파일(service docs 등)은 검사 제외
if echo "$FILE_PATH" | grep -q "${PROJECT_ROOT}/.claude/"; then
    exit 0
fi

# 소스 파일(.java, .ts, .tsx, .css) 읽기 시도 시 검사
if echo "$FILE_PATH" | grep -qE '\.(java|tsx?|css)$'; then
    # 상태 파일이 없거나 4시간 이상 지난 경우 차단
    if [ ! -f "$STATE_FILE" ] || [ -n "$(find "$STATE_FILE" -mmin +240 2>/dev/null)" ]; then
        rm -f "$STATE_FILE"
        echo "⛔ [CLAUDE.md 규칙] 소스 파일을 읽기 전에 먼저 .claude/docs/codebase-map.md 를 읽어야 합니다." >&2
        exit 2
    fi
fi

exit 0
