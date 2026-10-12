// k6 load test: browse catalogue, add to cart and place an order through the gateway.
// Run: k6 run -e BASE_URL=http://localhost:3000 load-tests/order-flow.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:3000';
const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
  stages: [{ duration: '30s', target: 10 }, { duration: '1m', target: 30 }, { duration: '20s', target: 0 }],
  thresholds: { http_req_failed: ['rate<0.02'], http_req_duration: ['p(95)<800'] },
};

export function setup() {
  const email = `k6-${Date.now()}@shopsphere.local`;
  const r = http.post(`${BASE}/api/auth/register`, JSON.stringify({ name: 'k6', email, password: 'Password123' }), { headers: JSON_HEADERS });
  return { token: r.json('accessToken') };
}

export default function (data) {
  const auth = { headers: { ...JSON_HEADERS, Authorization: `Bearer ${data.token}` } };
  const list = http.get(`${BASE}/api/products`);
  check(list, { 'catalogue 200': (r) => r.status === 200 });
  const product = list.json('0');
  check(http.post(`${BASE}/api/carts/items`, JSON.stringify({ productId: product.id, name: product.name, price: product.price, image: product.image, quantity: 1 }), auth),
    { 'cart 200': (r) => r.status === 200 });
  check(http.post(`${BASE}/api/orders`, JSON.stringify({ items: [{ productId: 'SKU-1004', quantity: 1 }] }), auth),
    { 'order accepted': (r) => r.status === 201 || r.status === 202 });
  sleep(1);
}
