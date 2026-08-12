import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 5,
  iterations: 5,
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // Génère un buffer de 10 Mo
  const size = 10 * 1024 * 1024; // 10 MB
  const buffer = new ArrayBuffer(size);
  const view = new Uint8Array(buffer);
  view.fill(65); // Remplit avec 'A'

  const data = {
    file: http.file(buffer, 'test-10mb.bin', 'application/octet-stream'),
  };

  let res = http.post(`${BASE_URL}/files/upload`, data);

  check(res, {
    'upload status is 200': (r) => r.status === 200,
    'no server error': (r) => r.status < 500,
  });
}
