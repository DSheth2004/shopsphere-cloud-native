#!/usr/bin/env bash
# End-to-end smoke test: health, register, cart, saga (happy + compensation), RBAC.
# Usage: scripts/smoke-test.sh [base-url]   (default http://localhost:3000)
set -euo pipefail
B=${1:-http://localhost:3000}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-Admin@12345}
j() { python3 -c "import sys,json;d=json.load(sys.stdin);print($1)"; }
post() { curl -fsS -X POST "$B$1" -H 'Content-Type: application/json' ${3:+-H "Authorization: Bearer $3"} -d "$2"; }
get() { curl -fsS "$B$1" ${2:+-H "Authorization: Bearer $2"}; }
wait_order() {
  for _ in $(seq 20); do
    s=$(get "/api/orders/$1" "$2" | j "d['status']")
    case $s in CONFIRMED|CANCELLED) echo "$s"; return;; esac; sleep 0.5
  done; echo TIMEOUT
}

echo "gateway: $(get /actuator/health | j "d['status']")"
for s in auth product inventory cart order payment notification; do printf "%s=%s " "$s" "$(get /status/$s | j "d['status']")"; done; echo
echo "products: $(get /api/products | j 'len(d)')"
T=$(post /api/auth/register "{\"name\":\"Smoke\",\"email\":\"smoke$RANDOM@shopsphere.local\",\"password\":\"Password123\"}" | j "d['accessToken']")
echo "cart without token: $(curl -s -o /dev/null -w '%{http_code}' "$B/api/carts") (expect 401)"
echo "cart: $(post /api/carts/items '{"productId":"SKU-1001","name":"Aurora","price":4999,"image":"x","quantity":1}' "$T" | j "d['itemCount']") item(s)"
O=$(post /api/orders '{"items":[{"productId":"SKU-1001","quantity":1}]}' "$T" | j "d['id']")
echo "happy order: $(wait_order "$O" "$T") (expect CONFIRMED)"
O=$(post /api/orders '{"items":[{"productId":"SKU-1002","quantity":7}]}' "$T" | j "d['id']")
echo "over card limit: $(wait_order "$O" "$T") (expect CANCELLED, stock released)"
echo "user creating product: $(curl -s -o /dev/null -w '%{http_code}' -X POST "$B/api/products" -H "Authorization: Bearer $T" -H 'Content-Type: application/json' -d '{"name":"x","price":1,"category":"y"}') (expect 403)"
A=$(post /api/auth/login "{\"email\":\"admin@shopsphere.local\",\"password\":\"$ADMIN_PASSWORD\"}" | j "d['accessToken']")
echo "admin created: $(post /api/products '{"name":"Smoke Kettle","price":1500,"category":"Home","image":"🫖"}' "$A" | j "d['id']")"
