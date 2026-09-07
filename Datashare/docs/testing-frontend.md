# Tests frontend

## 1. Pyramide de tests

```
        /\
       /E2E\     Cypress
      /------\
     / Vitest \  unitaires Angular
    /----------\
   /   JUnit    \ unitaires Spring
  /--------------\
```

## 2. Vitest (unitaires)

1. Commande : `npm test` dans `datashare_frontend`.
2. Couverture : `npm run test:coverage`.

![Test coverage](images\coverage-frontend-vitest.png)


## 3. Cypress (E2E)

### 3.1 Lancement

```bash
# Terminal 1 — front
npm start

# Terminal 2 — tests
npm run e2e:run
```

![Test cypress e2e](images\cypress-e2e-results_short.png)

### 3.2 Mode réel

| Mode | Commande | Backend requis |
|------|----------|----------------|
| Réel (défaut) | `npm run e2e:run` | Oui (`:8080` + PostgreSQL) |



1. En mode réel, les tests créent de vrais utilisateurs et fichiers en base.
2. Les pages objets se trouvent dans `cypress/pages/`.
3. Les scénarios sont dans `cypress/e2e/`.
