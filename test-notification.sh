#!/usr/bin/env bash
# notification-service SSE 및 읽음 처리 통합 테스트
# 사용법: ./test-notification.sh [BASE_URL]
# 예시: ./test-notification.sh http://localhost:8082

BASE_URL="${1:-http://localhost:8082}"
PASS=0
FAIL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

ok()   { echo -e "${GREEN}[PASS]${NC} $1"; PASS=$((PASS+1)); }
fail() { echo -e "${RED}[FAIL]${NC} $1"; FAIL=$((FAIL+1)); }
info() { echo -e "${BLUE}[INFO]${NC} $1"; }

echo ""
echo "=== notification-service 통합 테스트 ==="
echo "대상: $BASE_URL"
echo ""

# ─────────────────────────────────────────
# 1. GET /api/notifications
# ─────────────────────────────────────────
info "1. 알림 목록 조회 테스트"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/notifications")
if [ "$STATUS" = "200" ]; then
    ok "GET /api/notifications → $STATUS"
else
    fail "GET /api/notifications → $STATUS (200 expected)"
fi

# ─────────────────────────────────────────
# 2. SSE 스트림 Content-Type 확인
# ─────────────────────────────────────────
info "2. SSE 스트림 Content-Type 테스트"
# SSE는 GET만 지원하므로 write_out 옵션으로 content_type 확인
CONTENT_TYPE=$(curl -s --max-time 3 -o /dev/null -w "%{content_type}" \
    "$BASE_URL/api/notifications/stream" 2>/dev/null)
if echo "$CONTENT_TYPE" | grep -qi "text/event-stream"; then
    ok "GET /api/notifications/stream → Content-Type: $CONTENT_TYPE"
else
    fail "SSE Content-Type 확인 실패: '$CONTENT_TYPE' (text/event-stream expected)"
fi

# ─────────────────────────────────────────
# 3. 미읽음 카운트 조회
# ─────────────────────────────────────────
info "3. 미읽음 카운트 조회 테스트"
UNREAD_RESP=$(curl -s "$BASE_URL/api/notifications/unread-count")
if echo "$UNREAD_RESP" | grep -q '"count"'; then
    ok "GET /api/notifications/unread-count → $UNREAD_RESP"
else
    fail "미읽음 카운트 조회 실패: $UNREAD_RESP"
fi

# ─────────────────────────────────────────
# 4. 읽음 처리 테스트 (알림이 존재하는 경우)
# ─────────────────────────────────────────
info "4. 단건 읽음 처리 테스트"
FIRST_ID=$(curl -s "$BASE_URL/api/notifications" \
    | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')" 2>/dev/null)

if [ -n "$FIRST_ID" ]; then
    READ_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH \
        "$BASE_URL/api/notifications/$FIRST_ID/read")
    if [ "$READ_STATUS" = "200" ]; then
        ok "PATCH /api/notifications/$FIRST_ID/read → $READ_STATUS"
        # 읽음 상태 확인
        IS_READ=$(curl -s "$BASE_URL/api/notifications" \
            | python3 -c "import sys,json; d=json.load(sys.stdin); \
              item=[x for x in d if x['id']==$FIRST_ID]; \
              print(item[0]['isRead'] if item else 'not_found')" 2>/dev/null)
        if [ "$IS_READ" = "True" ] || [ "$IS_READ" = "true" ]; then
            ok "읽음 상태 반영 확인 (isRead=true)"
        else
            fail "읽음 상태 미반영: isRead=$IS_READ"
        fi
    else
        fail "PATCH /api/notifications/$FIRST_ID/read → $READ_STATUS (200 expected)"
    fi
else
    info "알림이 없어 단건 읽음 처리 스킵 (주문 생성 후 재테스트)"
fi

# ─────────────────────────────────────────
# 5. 일괄 읽음 처리
# ─────────────────────────────────────────
info "5. 일괄 읽음 처리 테스트"
ALL_READ_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH \
    "$BASE_URL/api/notifications/read-all")
if [ "$ALL_READ_STATUS" = "200" ]; then
    ok "PATCH /api/notifications/read-all → $ALL_READ_STATUS"
    # 미읽음 카운트 0 확인
    UNREAD_COUNT=$(curl -s "$BASE_URL/api/notifications/unread-count" \
        | python3 -c "import sys,json; print(json.load(sys.stdin)['count'])" 2>/dev/null)
    if [ "$UNREAD_COUNT" = "0" ]; then
        ok "일괄 읽음 후 미읽음 카운트 = 0"
    else
        fail "일괄 읽음 후 미읽음 카운트 = $UNREAD_COUNT (0 expected)"
    fi
else
    fail "PATCH /api/notifications/read-all → $ALL_READ_STATUS (200 expected)"
fi

# ─────────────────────────────────────────
# 6. SSE 이벤트 수신 확인 (백그라운드 청취 후 수동 트리거 필요)
# ─────────────────────────────────────────
info "6. SSE 실시간 이벤트 수신 테스트 (ping 이벤트)"
info "   3초간 SSE 스트림 청취 중..."
SSE_OUTPUT=$(curl -s --max-time 3 "$BASE_URL/api/notifications/stream" 2>/dev/null)
if echo "$SSE_OUTPUT" | grep -q "ping"; then
    ok "SSE ping 이벤트 수신 성공: $(echo "$SSE_OUTPUT" | head -1)"
elif echo "$SSE_OUTPUT" | grep -q "event:"; then
    ok "SSE 이벤트 수신 성공: $(echo "$SSE_OUTPUT" | head -2)"
else
    fail "SSE 이벤트 수신 실패 (응답 없음)"
fi

# ─────────────────────────────────────────
# 결과 요약
# ─────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e " 통과: ${GREEN}$PASS${NC}  실패: ${RED}$FAIL${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ $FAIL -gt 0 ]; then
    exit 1
fi
exit 0
