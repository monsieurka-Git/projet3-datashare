# Maintenance — DataShare

## 1. Architecture
- Backend : Spring Boot (8080)
- Frontend : Angular (4200)
- Base : PostgreSQL (5432)
- Stockage : dossier `uploads/`

## 2. Démarrage local
- PostgreSQL : local ou `docker compose up -d db`
- Backend : `./mvnw spring-boot:run`
- Frontend : `npm start`
- Vérifications : santé API, CORS, JWT, upload < 1 Go

## 3. Stockage & purge
- Fichiers binaires : `uploads/`
- Métadonnées : table `files`
- Purge automatique : job `ExpiredFileCleanupJob` chaque jour à 03:00

## 4. Sauvegarde
```
>pg_dump -U postgres datashare > d:\backup_datashare_$(Get-Date -Format "yyyyMMdd").sql
```
## 4.1 Restauration
```
>psql -U <user> datashare < backup_datashare_YYYYMMDD.sql
```
- Fréquence : quotidienne (base + fichiers)

## 4.2 Commandes de base postgresql
```
>psql -U postgres -d datashare
```
```
>select * from users;
```
```
>select * from files;
```

## 5. Maintenance GitHub
- Dependabot maintient les dépendances à jour.
- Pull Requests + reviews garantissent la qualité du code.
- GitHub Actions automatise build, tests et scans.
- Issues pour suivre bugs, tâches et améliorations.
- Nettoyage régulier des branches et mise à jour de la documentation.


## 6. Mises à jour
- Backend : `mvn versions:display-dependency-updates`
- Frontend : `npm outdated` / `npm update`
- Automatisation : Dependabot + Renovate (patch/minor auto, majors manuels)
- CI obligatoire avant merge

## 7. Dépannage
- CORS : origine front non autorisée
- 401 : JWT absent/expiré
- Upload refusé : extension interdite / > 1 Go
- Lien expiré : `expiresAt` dépassé
- Logs utiles : Spring, navigateur, Cypress

## 8. IA

Utilisation documentée dans le dépôt [ai-usage](ai-usage.md), revue [code-review-ia.md](code-review-ia.md) : binôme technique + revue humaine (secrets, tests, alignement API).
