#!/bin/bash
#
# Resilience Patterns Demo — Circuit Breaker, Retry, Timeout, Fallback
#
# Demonstrates that when the Catalogue service goes down:
#   1. Lending service retries the call (3 attempts)
#   2. Fallback fires and returns HTTP 503
#   3. Circuit breaker opens after repeated failures
#   4. Actuator endpoints show circuit breaker state
#   5. Circuit recovers when Catalogue comes back
#
# Prerequisites: identity, lending, and catalogue services running.
#   ./test-lending.sh should pass first to confirm baseline works.
#

GATEWAY="http://localhost:9920"
LENDING="http://localhost:9923"
CATALOGUE="http://localhost:9922"

GREEN="\033[0;32m"
RED="\033[0;31m"
BLUE="\033[0;34m"
YELLOW="\033[0;33m"
BOLD="\033[1m"
RESET="\033[0m"

PASS="${GREEN}[PASS]${RESET}"
FAIL="${RED}[FAIL]${RESET}"
INFO="${BLUE}[INFO]${RESET}"
WARN="${YELLOW}[WARN]${RESET}"

section() { echo -e "\n${BOLD}── $1 ──${RESET}\n"; }
check() {
  local label=$1 status=$2 expected=$3 body=$4
  if [ "$status" -eq "$expected" ] 2>/dev/null; then
    echo -e "$PASS $label (HTTP $status)"
  else
    echo -e "$FAIL $label (expected $expected, got $status)"
    [ -n "$body" ] && echo "       $body"
  fi
}

# ─────────────────────────────────────────────
section "SETUP — Register & Login"
# ─────────────────────────────────────────────

# Register (409 = already exists, that's fine)
curl -s -X POST "$GATEWAY/api/user/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"resilience-test","password":"pass123","email":"resilience@test.com"}' > /dev/null 2>&1

LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/api/user/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"resilience-test","password":"pass123"}')
LOGIN_BODY=$(echo "$LOGIN" | head -n1)
LOGIN_STATUS=$(echo "$LOGIN" | tail -n1)
check "Login" "$LOGIN_STATUS" 200

TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
USER_ID=$(echo "$LOGIN_BODY" | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
echo -e "$INFO User ID: $USER_ID"

if [ -z "$TOKEN" ] || [ -z "$USER_ID" ]; then
  echo -e "$FAIL Could not get token/userId. Are identity and gateway running?"
  exit 1
fi

# Get or create a book
BOOK_ID=$(curl -s "$GATEWAY/api/book-catalogue/books" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "import sys,json; books=json.load(sys.stdin); print(books[0]['id'] if books else '')" 2>/dev/null)

if [ -z "$BOOK_ID" ]; then
  BOOK=$(curl -s "$GATEWAY/api/book-catalogue/books" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -X POST \
    -d '{"isbn":"978-0-99-999999-0","title":"Resilience Test Book","description":"test","publicationYear":2024,"copies":3,"authors":["Test"],"categories":["Test"]}')
  BOOK_ID=$(echo "$BOOK" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
fi
echo -e "$INFO Book ID: $BOOK_ID"

# Clean up any active loans for this user
for LID in $(curl -s "$GATEWAY/api/lending/loans/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "import sys,json
try:
    for l in json.load(sys.stdin):
        if l['status']=='ACTIVE': print(l['id'])
except: pass" 2>/dev/null); do
  curl -s -X POST "$GATEWAY/api/lending/loans/$LID/return" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
done

# ─────────────────────────────────────────────
section "PHASE 1 — Baseline: Borrow with Catalogue UP"
# ─────────────────────────────────────────────

echo -e "$INFO Verifying catalogue service is reachable..."
CAT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$CATALOGUE/api/book-catalogue/books")
if [ "$CAT_STATUS" -eq 200 ]; then
  echo -e "$PASS Catalogue service is UP (HTTP $CAT_STATUS)"
else
  echo -e "$FAIL Catalogue service unreachable (HTTP $CAT_STATUS). Start it first."
  exit 1
fi

BORROW=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/api/lending/borrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
BORROW_STATUS=$(echo "$BORROW" | tail -n1)
BORROW_BODY=$(echo "$BORROW" | head -n1)
check "Borrow book (catalogue UP)" "$BORROW_STATUS" 201 "$BORROW_BODY"

# Return it so we can reuse the copy
LOAN_ID=$(echo "$BORROW_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
if [ -n "$LOAN_ID" ]; then
  curl -s -X POST "$GATEWAY/api/lending/loans/$LOAN_ID/return" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
fi

echo -e "$INFO Checking circuit breaker state (should be CLOSED)..."
CB_STATE=$(curl -s "$LENDING/actuator/circuitbreakers" 2>/dev/null)
echo -e "$INFO Circuit breakers: $CB_STATE"

# ─────────────────────────────────────────────
section "PHASE 2 — Stop Catalogue & Observe Failure"
# ─────────────────────────────────────────────

echo -e "${BOLD}${YELLOW}>>> ACTION REQUIRED: Stop the Catalogue service now <<<${RESET}"
echo -e "$INFO Waiting for catalogue to go down..."
echo -e "$INFO (If running with docker: ${BOLD}docker compose stop elib-catalogue${RESET})"
echo -e "$INFO (If running locally: kill the process on port 9922)"
echo ""
read -p "Press ENTER once catalogue is stopped..."

# Verify catalogue is down
CAT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "$CATALOGUE/api/book-catalogue/books" 2>/dev/null)
if [ "$CAT_STATUS" -eq 000 ] || [ "$CAT_STATUS" -eq 502 ] || [ "$CAT_STATUS" -eq 503 ]; then
  echo -e "$PASS Catalogue service confirmed DOWN"
else
  echo -e "$WARN Catalogue returned HTTP $CAT_STATUS — might still be running"
fi

echo ""
echo -e "$INFO Attempting to borrow with catalogue DOWN..."
echo -e "$INFO (Lending will retry 3 times, then circuit breaker fallback fires)"
echo ""

START=$(date +%s%N)
BORROW_FAIL=$(curl -s -w "\n%{http_code}" --max-time 30 -X POST "$GATEWAY/api/lending/borrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
END=$(date +%s%N)
ELAPSED_MS=$(( (END - START) / 1000000 ))

FAIL_STATUS=$(echo "$BORROW_FAIL" | tail -n1)
FAIL_BODY=$(echo "$BORROW_FAIL" | head -n1)
check "Borrow rejected — catalogue unavailable (503)" "$FAIL_STATUS" 503 "$FAIL_BODY"
echo -e "$INFO Response: $FAIL_BODY"
echo -e "$INFO Time taken: ${ELAPSED_MS}ms (includes retries)"

# Fire a few more to trigger circuit open
echo ""
echo -e "$INFO Sending more requests to trigger circuit breaker OPEN state..."
for i in 1 2 3 4 5; do
  R=$(curl -s -w "\n%{http_code}" --max-time 30 -X POST "$GATEWAY/api/lending/borrow" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
  S=$(echo "$R" | tail -n1)
  echo -e "$INFO   Request $i → HTTP $S"
done

# ─────────────────────────────────────────────
section "PHASE 3 — Observe Circuit Breaker State"
# ─────────────────────────────────────────────

echo -e "$INFO Checking circuit breaker state via actuator..."
echo ""

echo -e "${BOLD}GET /actuator/circuitbreakers${RESET}"
CB_STATE=$(curl -s "$LENDING/actuator/circuitbreakers" 2>/dev/null)
echo "$CB_STATE" | python3 -m json.tool 2>/dev/null || echo "$CB_STATE"

echo ""
echo -e "${BOLD}GET /actuator/health (circuit breaker details)${RESET}"
HEALTH=$(curl -s "$LENDING/actuator/health" 2>/dev/null)
echo "$HEALTH" | python3 -c "
import sys, json
try:
    h = json.load(sys.stdin)
    cb = h.get('components', {}).get('circuitBreakers', {})
    if cb:
        print(json.dumps(cb, indent=2))
    else:
        print('No circuit breaker health info found')
        print(json.dumps(h, indent=2))
except:
    print(sys.stdin.read())
" 2>/dev/null

echo ""
echo -e "${BOLD}GET /actuator/circuitbreakerevents (recent events)${RESET}"
CB_EVENTS=$(curl -s "$LENDING/actuator/circuitbreakerevents" 2>/dev/null)
echo "$CB_EVENTS" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    events = data.get('circuitBreakerEvents', [])
    for e in events[-10:]:
        print(f\"  {e.get('creationTime','')} | {e.get('type','')} | {e.get('circuitBreakerName','')}\")
    if not events:
        print('  (no events recorded)')
except:
    print(sys.stdin.read())
" 2>/dev/null

# ─────────────────────────────────────────────
section "PHASE 4 — Restart Catalogue & Observe Recovery"
# ─────────────────────────────────────────────

echo -e "${BOLD}${YELLOW}>>> ACTION REQUIRED: Start the Catalogue service again <<<${RESET}"
echo -e "$INFO (docker compose start elib-catalogue, or restart the process)"
echo ""
read -p "Press ENTER once catalogue is back up..."

# Wait for it to be healthy
echo -e "$INFO Waiting for catalogue to respond..."
for i in $(seq 1 15); do
  CAT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "$CATALOGUE/api/book-catalogue/books" 2>/dev/null)
  if [ "$CAT_STATUS" -eq 200 ]; then
    echo -e "$PASS Catalogue is back UP"
    break
  fi
  sleep 2
done

echo -e "$INFO Waiting for circuit breaker to transition to HALF_OPEN (up to 15s)..."
sleep 12

echo ""
echo -e "$INFO Attempting borrow again (should succeed if circuit recovered)..."
BORROW_RECOVER=$(curl -s -w "\n%{http_code}" -X POST "$GATEWAY/api/lending/borrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
RECOVER_STATUS=$(echo "$BORROW_RECOVER" | tail -n1)
RECOVER_BODY=$(echo "$BORROW_RECOVER" | head -n1)
check "Borrow succeeds after recovery" "$RECOVER_STATUS" 201 "$RECOVER_BODY"
echo -e "$INFO Response: $RECOVER_BODY"

# Clean up
LOAN_ID=$(echo "$RECOVER_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
if [ -n "$LOAN_ID" ]; then
  curl -s -X POST "$GATEWAY/api/lending/loans/$LOAN_ID/return" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
fi

echo ""
echo -e "${BOLD}Final circuit breaker state:${RESET}"
curl -s "$LENDING/actuator/circuitbreakers" 2>/dev/null | python3 -m json.tool 2>/dev/null

# ─────────────────────────────────────────────
section "SUMMARY"
# ─────────────────────────────────────────────

echo -e "${BOLD}Resilience patterns demonstrated:${RESET}"
echo ""
echo "  1. TIMEOUTS     — RestTemplate: 3s connect, 5s read"
echo "  2. RETRIES      — 3 attempts with 500ms wait (Resilience4j @Retry)"
echo "  3. CIRCUIT      — Opens at 50% failure rate over 10 calls,"
echo "     BREAKER        stays open 10s, then half-open with 3 test calls"
echo "  4. FALLBACK     — CatalogueClient fallback returns HTTP 503 with"
echo "                    user-friendly message instead of stack trace"
echo "  5. OBSERVABILITY— Actuator endpoints expose CB state + event log"
echo "  6. RECOVERY     — Circuit auto-closes when downstream recovers"
echo ""
echo -e "${BOLD}Gateway-level resilience:${RESET}"
echo "  - Circuit breaker filter on all 5 service routes"
echo "  - 5s timeout per route via TimeLimiter"
echo ""
echo "=== Done ==="
echo ""
