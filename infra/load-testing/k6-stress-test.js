import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp-up para 50 VUs
    { duration: '1m',  target: 200 },  // Carga sustentada de 200 VUs (~5.000 req/s)
    { duration: '30s', target: 500 },  // Pico de estresse de 500 VUs (~15.000 req/s)
    { duration: '20s', target: 0 },    // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% das requisições devem responder em menos de 200ms
    http_req_failed: ['rate<0.01'],    // Menos de 1% de falhas
  },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8085';

export default function () {
  // 1. Simula envio de métrica de telemetria
  const payload = JSON.stringify({
    nome: 'http.request.duration',
    valor: Math.random() * 100 + 10,
    timestamp: new Date().toISOString(),
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-API-KEY': 'ak_live_prod_9876543210abcdef',
    },
  };

  const res1 = http.post(`${BASE_URL}/api/v1/telemetry/hardware`, payload, params);
  check(res1, { 'status 200': (r) => r.status === 200 });

  // 2. Simula consulta de saúde das APIs
  const res2 = http.get(`${BASE_URL}/api/v1/telemetry/api-health`);
  check(res2, { 'status 200': (r) => r.status === 200 });

  sleep(0.1);
}
