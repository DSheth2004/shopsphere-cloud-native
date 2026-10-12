import http from 'k6/http';
import { check } from 'k6';
export const options = { stages: [{ duration: '30s', target: 10 }, { duration: '1m', target: 30 }, { duration: '20s', target: 0 }] };
export default function () { const r = http.get(`${__ENV.BASE_URL || 'http://localhost:8080'}/actuator/health`); check(r, { healthy: (x) => x.status === 200 }); }
