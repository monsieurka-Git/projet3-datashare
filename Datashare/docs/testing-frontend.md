# Tests frontend

## Vitest

```bash
cd datashare_frontend && npm test
```
## Coverage report from istanbul

```bash
cd datashare_frontend && npm run test:coverage
```
![Test coverage](images\coverage-frontend-vitest.png)

## Cypress — réel (PostgreSQL) vs mock

| Mode | Commande | Backend |
|------|----------|---------|
| **real** (défaut) | `npm run e2e:run` | Oui (:8080 + PG) |
| **mock** | `CYPRESS_MODE=mock npm run e2e:run` | Non |

```bash
# Terminal 1
npm start
# Terminal 2 — écrit en base réelle
npm run e2e:run
```

![Test cypress e2e](images\cypress-e2e-results_short.png)

Voir [cypress_e2e.md](cypress_e2e.md) pour le détail du switch et les correctifs API.



