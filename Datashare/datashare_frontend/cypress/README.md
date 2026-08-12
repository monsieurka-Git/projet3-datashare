# Tests E2E Cypress — DataShare

## Prérequis

```bash
cd datashare_frontend
npm install
npx cypress install
```

Le frontend doit être accessible sur **http://localhost:4200**.  
Les tests **mockent l’API** (`cy.intercept`) : le backend Spring n’est **pas obligatoire** pour les faire passer.

## Lancer les tests

```bash
# UI interactive
npm run e2e

# Mode headless (CI)
npm run e2e:run

# Démarre ng serve + lance Cypress automatiquement
npm run e2e:ci
```

## Couverture E2E

```bash
npm run e2e:coverage
```

Rapport HTML généré dans :

```
datashare_frontend/coverage-e2e/index.html
```

### Comment ça marche

1. `@cypress/code-coverage` collecte `window.__coverage__` pendant les runs.
2. `nyc` produit le rapport HTML / LCOV dans `coverage-e2e/`.

> **Note Angular 22 (esbuild)** : pour une instrumentation Istanbul complète du bundle,
> il faut un build instrumenté (plugin Istanbul / babel). Les specs E2E valident
> surtout les **parcours utilisateur** (US01–US07). La couverture *unitaire*
> reste mesurée via `npm run test:coverage` (Vitest).

## Scénarios (TESTING.md)

| Fichier | US | Description |
|---------|-----|-------------|
| `01-auth.cy.ts` | US03, US04 | Inscription + connexion + JWT |
| `02-upload-download.cy.ts` | US01, US02 | Upload, Mes fichiers, download |
| `03-delete-file.cy.ts` | US06 | Modale + suppression |
| `04-anonymous-upload.cy.ts` | US07 | Welcome → upload anonyme |

## Contre un vrai backend

1. Démarrer Spring Boot (`localhost:8080`)
2. Commenter ou adapter les `cy.intercept` dans `cypress/support/commands.ts`
3. Relancer `npm run e2e:run`
