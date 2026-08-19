# Maintenance — DataShare

## Architecture
- Backend : Spring Boot (8080)
- Frontend : Angular (4200)
- Base : PostgreSQL (5432)
- Stockage : dossier `uploads/`

## Démarrage local
- PostgreSQL : local ou `docker compose up -d db`
- Backend : `./mvnw spring-boot:run`
- Frontend : `npm start`
- Vérifications : santé API, CORS, JWT, upload < 1 Go

## Stockage & purge
- Fichiers binaires : `uploads/`
- Métadonnées : table `files`
- Purge automatique : job `ExpiredFileCleanupJob` chaque jour à 03:00

## Sauvegarde
```
>pg_dump -U postgres datashare > d:\backup_datashare_$(Get-Date -Format "yyyyMMdd").sql
```
# Restauration
```
>psql -U <user> datashare < backup_datashare_YYYYMMDD.sql
```
- Fréquence : quotidienne (base + fichiers)

## Commandes de base postgresql
```
>psql -U postgres -d datashare
```
```
>select * from users;
```
```
>select * from files;
```
```
>DELETE FROM users WHERE email LIKE 'auth%' OR email LIKE 'e2e%';
```



## Mises à jour
- Backend : `mvn versions:display-dependency-updates`
- Frontend : `npm outdated` / `npm update`
- Automatisation : Dependabot + Renovate (patch/minor auto, majors manuels)
- CI obligatoire avant merge

## Dépannage
- CORS : origine front non autorisée
- 401 : JWT absent/expiré
- Upload refusé : extension interdite / > 1 Go
- Lien expiré : `expiresAt` dépassé
- Logs utiles : Spring, navigateur, Cypress

## IA

Utilisation documentée dans le dépôt [ai-usage](ai-usage.md), revue [code-review-ia.md](code-review-ia.md) : binôme technique + revue humaine (secrets, tests, alignement API).
