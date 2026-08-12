import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 20,                 // 20 requêtes simultanées
  duration: '20s',
  thresholds: {
    http_req_duration: ['p(95)<500'], // p95 < 500 ms
  },
};

const BASE_URL = 'http://localhost:8080/api/files/metadata';

export default function () {
  let res = http.get(`${BASE_URL}/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee`);

  check(res, {
    'metadata status is 200': (r) => r.status === 200,
    'no server error': (r) => r.status < 500,
  });
}
