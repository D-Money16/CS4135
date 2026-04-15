#!/bin/bash

BASE="http://localhost:9920"
PASS="\033[0;32m[PASS]\033[0m"
FAIL="\033[0;31m[FAIL]\033[0m"
INFO="\033[0;34m[INFO]\033[0m"
ISBN="978-0-13-468599-2"

check() {
  local label=$1
  local status=$2
  local expected=$3
  local body=$4
  if [ "$status" -eq "$expected" ]; then
    echo -e "$PASS $label (HTTP $status)"
  else
    echo -e "$FAIL $label (expected $expected, got $status)"
    echo "       $body"
  fi
}

echo ""
echo "=== elib Lending Service Test ==="
echo ""

# 1. Register (409 is fine — user already exists)
echo -e "$INFO Registering user..."
REGISTER=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/user/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"lendtest","password":"pass123","email":"lendtest@test.com"}')
REGISTER_BODY=$(echo "$REGISTER" | head -n1)
REGISTER_STATUS=$(echo "$REGISTER" | tail -n1)
if [ "$REGISTER_STATUS" -eq 201 ]; then
  echo -e "$PASS Register user (HTTP 201)"
  USER_ID=$(echo "$REGISTER_BODY" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
elif [ "$REGISTER_STATUS" -eq 409 ]; then
  echo -e "$INFO User already exists (409), will get ID from login"
else
  echo -e "$FAIL Register user (expected 201, got $REGISTER_STATUS)"
  echo "       $REGISTER_BODY"
fi

# 2. Login — always login to get token + userId
echo ""
echo -e "$INFO Logging in..."
LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/user/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"lendtest","password":"pass123"}')
LOGIN_BODY=$(echo "$LOGIN" | head -n1)
LOGIN_STATUS=$(echo "$LOGIN" | tail -n1)
check "Login" "$LOGIN_STATUS" 200 "$LOGIN_BODY"

TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo -e "$INFO Token: ${TOKEN:0:40}..."

# Get userId from login response if we didn't get it from register
LOGIN_USER_ID=$(echo "$LOGIN_BODY" | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
if [ -n "$LOGIN_USER_ID" ]; then
  USER_ID="$LOGIN_USER_ID"
fi
echo -e "$INFO User ID: $USER_ID"

if [ -z "$TOKEN" ]; then
  echo -e "$FAIL Could not extract token, aborting"
  exit 1
fi
if [ -z "$USER_ID" ]; then
  echo -e "$FAIL Could not extract user ID, aborting"
  exit 1
fi

# 3. Add book (if already exists, look it up instead)
echo ""
echo -e "$INFO Adding book..."
BOOK=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/book-catalogue/books" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"isbn\": \"$ISBN\",
    \"title\": \"Clean Code2\",
    \"description\": \"A handbook of agile software craftsmanship\",
    \"publicationYear\": 2008,
    \"copies\": 6,
    \"authors\": [\"Robert Martin\"],
    \"categories\": [\"Programming\"]
  }")
BOOK_BODY=$(echo "$BOOK" | head -n1)
BOOK_STATUS=$(echo "$BOOK" | tail -n1)

if [ "$BOOK_STATUS" -eq 200 ]; then
  echo -e "$PASS Add book (HTTP 200)"
  # Extract book ID using python to handle JSON field ordering
  BOOK_ID=$(echo "$BOOK_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
else
  echo -e "$INFO Add book returned $BOOK_STATUS, searching for existing book by ISBN..."
  ALL_BOOKS=$(curl -s "$BASE/api/book-catalogue/books" \
    -H "Authorization: Bearer $TOKEN")
  BOOK_ID=$(echo "$ALL_BOOKS" | python3 -c "
import sys, json
for b in json.load(sys.stdin):
    if b.get('isbn') == '$ISBN':
        print(b['id'])
        break
" 2>/dev/null)
  if [ -n "$BOOK_ID" ]; then
    echo -e "$INFO Found existing book"
  fi
fi

echo -e "$INFO Book ID: $BOOK_ID"

if [ -z "$BOOK_ID" ]; then
  echo -e "$FAIL Could not extract book ID, aborting"
  exit 1
fi

# 4. Cleanup — return any active loans for this user before testing
echo ""
echo -e "$INFO Cleaning up old loans..."
LOANS_CLEANUP=$(curl -s "$BASE/api/lending/loans/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN")
CLEANED=0
for OLD_ID in $(echo "$LOANS_CLEANUP" | python3 -c "
import sys, json
try:
    for loan in json.load(sys.stdin):
        if loan.get('status') == 'ACTIVE':
            print(loan['id'])
except: pass
" 2>/dev/null); do
  curl -s -X POST "$BASE/api/lending/loans/$OLD_ID/return" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
  CLEANED=$((CLEANED + 1))
done
echo -e "$INFO Returned $CLEANED old active loans"

# 5. Borrow book
echo ""
echo -e "$INFO Borrowing book..."
BORROW=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/lending/borrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
BORROW_BODY=$(echo "$BORROW" | head -n1)
BORROW_STATUS=$(echo "$BORROW" | tail -n1)
check "Borrow book" "$BORROW_STATUS" 201 "$BORROW_BODY"

LOAN_ID=$(echo "$BORROW_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
echo -e "$INFO Loan ID: $LOAN_ID"

# 6. View user loans
echo ""
echo -e "$INFO Fetching user loans..."
LOANS=$(curl -s -w "\n%{http_code}" "$BASE/api/lending/loans/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN")
LOANS_BODY=$(echo "$LOANS" | head -n1)
LOANS_STATUS=$(echo "$LOANS" | tail -n1)
check "Get user loans" "$LOANS_STATUS" 200 "$LOANS_BODY"
LOAN_COUNT=$(echo "$LOANS_BODY" | python3 -c "import sys,json; print(len([l for l in json.load(sys.stdin) if l['status']=='ACTIVE']))" 2>/dev/null)
echo -e "$INFO Active loans: $LOAN_COUNT"

# 7. Return book
echo ""
echo -e "$INFO Returning book..."
RETURN=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/lending/loans/$LOAN_ID/return" \
  -H "Authorization: Bearer $TOKEN")
RETURN_BODY=$(echo "$RETURN" | head -n1)
RETURN_STATUS=$(echo "$RETURN" | tail -n1)
check "Return book" "$RETURN_STATUS" 200 "$RETURN_BODY"

# 8. Try to return same loan again (should fail)
echo ""
echo -e "$INFO Returning same loan again (should fail)..."
RETURN2=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/lending/loans/$LOAN_ID/return" \
  -H "Authorization: Bearer $TOKEN")
RETURN2_BODY=$(echo "$RETURN2" | head -n1)
RETURN2_STATUS=$(echo "$RETURN2" | tail -n1)
check "Double return rejected" "$RETURN2_STATUS" 400 "$RETURN2_BODY"

# 9. Test max 5 loans — borrow 5 times then a 6th should fail
echo ""
echo -e "$INFO Testing max 5 loans limit..."
for i in 1 2 3 4 5; do
  R=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/lending/borrow" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
  STATUS=$(echo "$R" | tail -n1)
  check "Borrow $i/5" "$STATUS" 201 "$(echo "$R" | head -n1)"
done

echo ""
echo -e "$INFO Attempting 6th borrow (should be rejected with 422)..."
R6=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/lending/borrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"userId\":\"$USER_ID\",\"bookId\":\"$BOOK_ID\"}")
R6_BODY=$(echo "$R6" | head -n1)
R6_STATUS=$(echo "$R6" | tail -n1)
check "6th borrow rejected (422)" "$R6_STATUS" 422 "$R6_BODY"
echo -e "$INFO Response: $R6_BODY"

# 10. Cleanup — return the 5 loans we just created
echo ""
echo -e "$INFO Cleaning up test loans..."
LOANS_FINAL=$(curl -s "$BASE/api/lending/loans/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN")
for AID in $(echo "$LOANS_FINAL" | python3 -c "
import sys, json
for loan in json.load(sys.stdin):
    if loan.get('status') == 'ACTIVE':
        print(loan['id'])
" 2>/dev/null); do
  curl -s -X POST "$BASE/api/lending/loans/$AID/return" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
done
echo -e "$INFO Cleanup done"

echo ""
echo "=== Done ==="
echo ""
