# AI_USAGE.md — Usage de l’IA dans le développement

## Posture

L’IA (assistant de code) a été utilisée comme **binôme technique**, pas comme substitut à la conception ni à la validation.

## Tâches confiées

- Structuration initiale Spring Boot / Angular
- Rédaction de documentation (TESTING, SECURITY, etc.)
- Aide au diagnostic (CORS, JWT, tests Cypress/Vitest)
- Génération de squelettes de tests unitaires

## Supervision humaine

- Revue systématique du code généré
- Ajustements sécurité (secrets externalisés, validation upload)
- Exécution réelle des tests avant livraison
- Alignement des endpoints doc ↔ code (ex. POST download)

## Limites

- L’IA peut proposer des chemins ou noms de fichiers incorrects
- Les tests générés doivent être exécutés et corrigés (ex. mock localStorage)
- Les décisions d’architecture restent de la responsabilité de l’étudiant
