# Cypress DataShare — mode réel / mock

## Prérequis mode **real** (défaut)

1. PostgreSQL (`docker compose up -d`)
2. Backend : `cd datashare_backend && ./mvnw spring-boot:run` (:8080)
3. Frontend : `cd datashare_frontend && npm start` (:4200)

```bash
cd datashare_frontend
npm run e2e:run
# équivalent
CYPRESS_MODE=real npm run e2e:run
```

Les tests créent de **vrais** utilisateurs (`e2e_*@datashare.test`) et fichiers en base.

## Mode **mock** (pas de backend)

```bash
CYPRESS_MODE=mock npm run e2e:run
```

Specs `cypress/e2e/mock/*` + intercepts ; specs real sont **skipped**.

## Switch résumé

| Variable | Effet |
|----------|--------|
| `CYPRESS_MODE=real` | API + PostgreSQL (défaut) |
| `CYPRESS_MODE=mock` | `cy.intercept` uniquement |
| `CYPRESS_API_URL` | Override URL API (défaut `http://localhost:8080`) |

## Corrections vs ancienne suite « real »

| Problème | Correctif |
|----------|-----------|
| Mot de passe `123456` | `password1` (≥ 8 car.) |
| `window.localStorage` hors navigateur | `cy.window()` / `onBeforeLoad` |
| `cy.request('/api/...')` sans host | URL absolue `apiUrl` |
| Download GET | **POST** `/api/files/download/{token}` |
| Delete `/api/files/{id}` | **DELETE** `/api/files/info/{id}` |
| Bouton « Envoyer » | **Téléverser** |
| Route `/dashboard` | **`/home`** |
| Email fixe déjà pris | Email unique `e2e_${Date.now()}@…` |
