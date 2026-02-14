import http from 'k6/http';
import { check, sleep } from 'k6';

const TOTAL = 6_000_000;
const PER_DAY = 2_000_000;

export const options = {
  scenarios: {
    ingest: {
      executor: 'shared-iterations',
      vus: 3000,
      iterations: TOTAL,
      maxDuration: '30m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<200'],
  },
};

function createdAtForGlobalIter(globalIter) {
  // 0..4_999_999 => dayIndex=0 => 13-е
  // 5_000_000..9_999_999 => dayIndex=1 => 14-е
  // 10_000_000..14_999_999 => dayIndex=2 => 15-е
  let dayIndex = Math.floor(globalIter / PER_DAY);
  if (dayIndex > 2) dayIndex = 2;

  const day = 13 + dayIndex; // 13,14,15

  const msInDay = 24 * 60 * 60 * 1000;
  const offset = globalIter % msInDay;

  const baseUtc = Date.UTC(2026, 1, day, 0, 0, 0); // month=1 => Feb
  return new Date(baseUtc + offset).toISOString();
}

export default function () {
  const createdAt = createdAtForGlobalIter(__ITER);

  const payload = JSON.stringify({
    symbol: 'BTC',
    price: 1000 + Math.random() * 100000,
    volume: Math.floor(Math.random() * 100) + 1,
    createdAt,
  });

  const res = http.post('http://localhost:8080/api/v1/ticks', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'status is 200/204': (r) => r.status === 200 || r.status === 204 });
}
