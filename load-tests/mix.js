import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        mixed: { executor: 'constant-vus', vus: 300, duration: '60s' },
    },
    thresholds: { http_req_failed: ['rate<0.01'] },
};

function nowRangeSeconds(sec) {
    const to = new Date();
    const from = new Date(Date.now() - sec * 1000);
    return { from: from.toISOString(), to: to.toISOString() };
}

function todayPartition() {
    const d = new Date();
    return d.toISOString().slice(0, 10); // YYYY-MM-DD
}

export default function () {
    const r = Math.random();

    // 70% ingest
    if (r < 0.70) {
        const payload = JSON.stringify({
            symbol: 'BTC',
            price: 1000 + Math.random() * 100000,
            volume: Math.floor(Math.random() * 100) + 1,
            createdAt: new Date().toISOString(),
        });

        const res = http.post(
            'http://localhost:8080/api/v1/ticks',
            payload,
            {
                headers: { 'Content-Type': 'application/json' },
                tags: { name: 'POST /ticks' },
            }
        );

        check(res, { 'POST ok': (x) => x.status === 200 || x.status === 204 });
        return;
    }

    // 20% select
    if (r < 0.90) {
        const { from, to } = nowRangeSeconds(30);
        const url = `http://localhost:8080/api/v1/ticks?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&limit=200`;

        const res = http.get(url, { tags: { name: 'GET /ticks' } });

        check(res, { 'GET ok': (x) => x.status === 200 });
        return;
    }

    // 10% update
    {
        const { from, to } = nowRangeSeconds(30);
        const url = `http://localhost:8080/api/v1/ticks/volume?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&delta=1`;

        const res = http.patch(url, null, {
            tags: { name: 'PATCH /ticks/volume' },
        });

        check(res, { 'PATCH ok': (x) => x.status === 200 || x.status === 204 });
    }
}
