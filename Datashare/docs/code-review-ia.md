## Rapport de revue technique du code — assistance IA

Document exigé par la fiche d’autoévaluation (*Assurer la performance, la conformité et la maintenance*).

## Périmètre revu avec l’IA

- Backend Spring Boot (controllers, services, JWT, purge)
- Frontend Angular (pages, services, guards, interceptor)
- Tests (JUnit, Vitest, Cypress)
- Configuration (`application.yml`, secrets)

## Points validés

| Thème | Constat | Action |
|-------|---------|--------|
| Secrets | JWT / BDD en clair dans yml | Externalisation `${JWT_SECRET}`, `${DB_PASSWORD}` |
| Download API | Doc GET vs code POST | Alignement doc + OpenAPI sur **POST** `/api/files/download/{token}` |
| Tests FE | Échecs localStorage (jsdom) | `src/test-setup.ts` + specs corrigés |
| Cypress | Uniquement mocks | Ajout smoke API réelle `05-real-api-smoke.cy.ts` + POM classes |
| Purge | Cron quotidien | Passage **horaire** `0 0 * * * *` |
| Couches back | Présentes | controller / service / repository respectés |
| Accessibilité | Partielle | `aria-label`, `role="banner"` sur header |

## Risques résiduels (acceptés pour le MVP)

- JWT en `localStorage` (sensible XSS) — mitigation : pas de HTML non échappé utilisateur
- Stockage fichiers local (pas d’antivirus) — liste noire d’extensions côté serveur
- E2E majoritairement mockés — smoke réel documenté pour la soutenance

## Conclusion de la revue

Le code est **maintenable et exploitable** pour le périmètre US01–US10. Les écarts bloquants relevés en soutenance (tests FE, cohérence doc, secrets) ont été traités. La suite de travail recommandée : axe-core en CI, cookie httpOnly en évolution sécurité.
