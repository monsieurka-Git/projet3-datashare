# Guide de Maintenance — DataShare

## 1. Vue d’ensemble

DataShare est une application de partage de fichiers temporaire composée de :

| Composant | Technologie | Port par défaut |
|-----------|-------------|-----------------|
| Backend API | Spring Boot 3.4 / Java 17+ | `8080` |
| Frontend | Angular 22 | `4200` |
| Base de données | PostgreSQL | `5432` |
| Stockage fichiers | Dossier local `uploads/` | — |

Ce document décrit les opérations de maintenance courantes, le déploiement, la sauvegarde et le dépannage.

---

## 2. Prérequis d’environnement

- **JDK** 17 ou supérieur
- **Maven** 3.9+ (ou `./mvnw`)
- **Node.js** 20+ et **npm** 10+
- **PostgreSQL** 14+
- Variables / fichiers de config :
  - `datashare_backend/src/main/resources/application.properties` (ou `.yml`)
  - secret JWT (`security.jwt.secret`) — **ne jamais committer un secret de production**

---

## 3. Démarrage local

### 3.1 Base de données

```bash
# Datashare connexion postgresql
psql -U postgres -d datashare
# Lister les tables
\d
```

Ou avec Docker :

```bash
docker compose up -d db
```

### 3.2 Backend

```bash
cd datashare_backend
./mvnw spring-boot:run
# API : http://localhost:8080
# Swagger (si activé) : http://localhost:8080/swagger-ui.html
```

### 3.3 Frontend

```bash
cd datashare_frontend
npm install
npm start
# App : http://localhost:4200
```

### 3.4 Vérifications rapides

| Contrôle | Action |
|----------|--------|
| Santé API | GET http://localhost:8080/api/auth/... ou Swagger |
| CORS | Origine front `http://localhost:4200` autorisée |
| JWT | Login → token dans `localStorage` (`token`, `userId`) |
| Upload | Fichier < 1 Go, extension autorisée |

---

## 4. Structure du projet

```
datashare/
├── datashare_backend/
│   ├── src/main/java/com/datashare/backend/
│   │   ├── controller/     # REST
│   │   ├── service/        # Métier
│   │   ├── repository/     # JPA
│   │   ├── model/          # Entités
│   │   ├── dto/
│   │   ├── JWT/
│   │   ├── exception/
│   │   ├── scheduler/      # Purge US10
│   │   └── config/
│   ├── src/test/
│   ├── pom.xml
    └── docker-compose.yml
├── datashare_frontend/
│   ├── src/app/
│   │   ├── pages/          # login, register, home, upload, download, landing
│   │   ├── services/
│   │   ├── components/
│   │   ├── guards/
│   │   └── interceptors/
│   ├── cypress/            # E2E
│   └── package.json
├── .github/
│   └── dependabot.yml      # Mises à jour auto Dependabot
├── renovate.json           # Mises à jour auto Renovate
└── docs/
    ├── TESTING.md
    ├── MAINTENANCE.md
    ├── PERF.md
    └── SECURITY.md
```

---

## 5. Configuration sensible

### 5.1 Backend (`application.properties` / profils)

Paramètres typiques à externaliser en production :

| Clé | Description |
|-----|-------------|
| `spring.datasource.*` | URL, user, password PostgreSQL |
| `security.jwt.secret` | Secret HMAC (≥ 32 caractères) |
| `security.jwt.expiration` | Durée de vie du token (ms) |
| Dossier `uploads/` | Chemin absolu recommandé en prod |

**Bonnes pratiques :**

- Utiliser des variables d’environnement ou un coffre (Vault, secrets CI)
- Profils Spring : `application-dev.properties` / `application-prod.properties`
- Ne pas versionner les secrets

### 5.2 Frontend

- URL API : services Angular (`http://localhost:8080/api` en dev)
- En production : configurer l’URL via `environment.prod.ts` ou variables de build

---

## 6. Stockage des fichiers

- Les binaires sont stockés sous **`uploads/`** (chemin relatif au working directory du process Java, sauf reconfiguration).
- Les métadonnées (token, expiration, hash mot de passe, tags, `ownerId`) sont en **PostgreSQL** (table `files`).
- **US10** : job planifié (`ExpiredFileCleanupJob`) tous les jours à **03:00** — suppression disque + ligne BDD.

### Maintenance stockage

```bash
# Taille du dossier
du -sh uploads/

# Fichiers orphelins (présents disque, absents BDD) : script d’audit manuel recommandé
# Ne jamais supprimer uploads/ sans backup si des liens encore valides existent
```

---

## 7. Sauvegarde et restauration

### 7.1 Base de données

```bash
# Sauvegarde
pg_dump -U <user> datashare > backup_datashare_$(date +%Y%m%d).sql

# Restauration
psql -U <user> datashare < backup_datashare_YYYYMMDD.sql
```

### 7.2 Fichiers

```bash
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/
```

### 7.3 Fréquence recommandée

| Élément | Fréquence indicative |
|---------|----------------------|
| PostgreSQL | Quotidienne |
| `uploads/` | Quotidienne ou continue (rsync) |
| Secrets / config | À chaque changement (coffre) |

---

## 8. Mises à jour et dépendances

### 8.1 Mise à jour manuelle

#### Backend

```bash
cd datashare_backend
./mvnw versions:display-dependency-updates
./mvnw clean verify
```

#### Frontend

```bash
cd datashare_frontend
npm outdated
npm update
npm test
npm run e2e:run
```

**Règle :** ne jamais monter de version majeure (Spring Boot, Angular) sans exécuter la batterie de tests (`TESTING.md`).

### 8.2 Automatisation — Dependabot (GitHub)

[Dependabot](https://docs.github.com/en/code-security/dependabot) ouvre des **pull requests** automatiques lorsque des dépendances npm, Maven ou GitHub Actions ont une mise à jour.

**Fichier de configuration :** [`.github/dependabot.yml`](../.github/dependabot.yml) à la racine du dépôt.

| Écosystème | Répertoire | Fréquence proposée |
|------------|------------|--------------------|
| `npm` | `/datashare_frontend` | Hebdomadaire (lundi) |
| `maven` | `/datashare_backend` | Hebdomadaire (lundi) |
| `github-actions` | `/` | Mensuelle |

**Contenu utile de la config DataShare :**

- Limite de PR ouvertes (`open-pull-requests-limit`)
- Labels `dependencies`, `frontend`, `backend`, `ci`
- Groupes : packages `@angular/*`, écosystème Spring
- Ignore des **majors** Angular / Spring Boot (traitement manuel)

**Bonnes pratiques Dependabot :**

- Activer les PR groupées (`groups`) pour limiter le bruit (patch / minor)
- Exiger les checks CI (tests unitaires + build) avant merge
- Traiter les **majors** manuellement (changelog + tests E2E)
- Ne pas merger une PR de sécurité sans lire l’advisory

**Activation GitHub :** *Settings → Code security → Dependabot → Enable*, ou simple présence de `.github/dependabot.yml` sur la branche par défaut.

### 8.3 Automatisation — Renovate

[Renovate](https://docs.renovatebot.com/) est une alternative (ou un complément) à Dependabot, souvent plus configurable (groupes, calendriers, dashboard, règles fines).

**Fichier de configuration :** [`renovate.json`](../renovate.json) à la racine du dépôt.

| Fonction | Intérêt pour DataShare |
|----------|------------------------|
| Multi-écosystème (`npm`, `maven`) | Un seul bot pour front + back |
| `packageRules` | Grouper Angular, Spring Boot, Cypress |
| `schedule` | PR hors heures de démo / sprint (ex. lundi matin) |
| `prConcurrentLimit` | Éviter une avalanche de PR |
| Labels `dependencies` / `security` | Tri dans le board |
| `:semanticCommits` | Messages de commit type Conventional Commits |
| `vulnerabilityAlerts` | Priorisation des correctifs de sécurité |

**Activation :**

1. Installer l’application **Renovate** sur le dépôt GitHub ou GitLab, **ou**
2. Utiliser un workflow self-hosted (`renovatebot/github-action`)

**Exemple de principe de configuration (extrait) :**

```json
{
  "extends": ["config:recommended", ":dependencyDashboard", ":semanticCommits"],
  "timezone": "Europe/Paris",
  "schedule": ["before 9am on monday"],
  "prConcurrentLimit": 5,
  "packageRules": [
    {
      "matchPackagePatterns": ["^@angular/"],
      "groupName": "angular",
      "matchUpdateTypes": ["minor", "patch"]
    },
    {
      "matchPackagePatterns": ["^org\\.springframework"],
      "groupName": "spring",
      "matchUpdateTypes": ["minor", "patch"]
    }
  ]
}
```

### 8.4 Processus commun Dependabot / Renovate

```text
PR dépendances → CI verte (mvn test + npm test) → revue humaine → merge
```

| Type de mise à jour | Traitement recommandé |
|---------------------|------------------------|
| Patch / minor | Revue rapide si CI verte |
| Major (Angular, Spring Boot) | Note dans l’historique (section 14) + tests E2E Cypress |
| Sécurité (CVE) | Priorité haute, lecture de l’advisory obligatoire |

**Ne pas** activer l’auto-merge non contrôlé sur les majors framework.

---

## 9. Logs et supervision

| Source | Contenu utile |
|--------|----------------|
| Logs Spring | Upload OK, erreurs métier, purge US10, JWT |
| Console navigateur | Erreurs CORS, 401/403, échecs HttpClient |
| Cypress screenshots | `cypress/screenshots/` en cas d’échec E2E |

Niveaux conseillés :

- **PROD** : `INFO` pour le métier, `WARN` / `ERROR` pour les échecs
- Éviter de logger les mots de passe, tokens complets ou contenus de fichiers

---

## 10. Tâches planifiées

| Tâche | Classe | Planification | Action |
|-------|--------|---------------|--------|
| Purge fichiers expirés | `ExpiredFileCleanupJob` | Cron `0 0 3 * * *` (03:00) | Supprime fichiers expirés (disque + BDD) |

Vérifier dans les logs au redémarrage que `@EnableScheduling` est actif (`BackendApplication`).

---

## 11. Dépannage courant

| Symptôme | Causes probables | Actions |
|----------|------------------|---------|
| CORS / status 0 | Backend arrêté, origine non autorisée | Vérifier SecurityConfig + CORS + port 8080 |
| 401 sur `/api/files` | JWT absent / expiré | Reconnexion ; vérifier interceptor Angular |
| Upload refusé | Extension interdite, taille > 1 Go | Message UI + règles `FileService` |
| Download « lien expiré » | `expiresAt` dépassé | Nouveau upload ; vérifier horloge serveur |
| « Téléchargement en cours » bloqué | État UI / réponse blob erreur | Voir correctifs download + Network tab |
| Suppression en double-clic | Modale non fermée immédiatement | Comportement corrigé (fermeture optimiste) |
| Compilation Lombok | Annotation processing | Préférer getters/setters explicites déjà en place |
| Cypress ne démarre pas | Binaire manquant | `npx cypress install` |

---

## 12. Déploiement (indicatif)

1. Build backend : `./mvnw -DskipTests package` → `target/*.jar`
2. Build frontend : `npm run build` → `dist/`
3. Servir le front (Nginx, CDN) et proxy `/api` vers le JAR Spring
4. Configurer PostgreSQL, volume persistant pour `uploads/`, secrets JWT
5. HTTPS obligatoire en production
6. Exécuter les migrations / schéma JPA (`ddl-auto` : préférer `validate` + Flyway/Liquibase en prod)

---

## 13. Contacts et responsabilités (à compléter)

| Rôle | Responsabilité |
|------|----------------|
| Maintainer backend | API, BDD, stockage, jobs |
| Maintainer frontend | UI Angular, Cypress |
| Ops / DevOps | Déploiement, backups, monitoring, bots Dependabot/Renovate |

---

## 14. Historique des versions de maintenance

| Date | Version app | Notes |
|------|-------------|-------|
| 2026-08 | US07–US10 + tests | Upload anonyme, tags, purge, JaCoCo, Cypress |
| 2026-08 | Maintenance deps | Intégration Dependabot (`.github/dependabot.yml`) et Renovate (`renovate.json`) |
