import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 10,          // 10 utilisateurs simultanés
  duration: '30s',  // pendant 30 secondes
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
  // 1. Login
  let loginRes = http.post(`${BASE_URL}/auth/login`, {
    email: 'user@datashare.test',
    password: '123456'
  });

  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
  });

  const token = loginRes.json('token');

  // 2. Liste des fichiers
  let filesRes = http.get(`${BASE_URL}/files`, {
    headers: { Authorization: `Bearer ${token}` }
  });

  check(filesRes, {
    'files status is 200': (r) => r.status === 200,
    'no server error': (r) => r.status < 500,
  });

  sleep(1);
}
