# DataShare

Application fullstack de **partage de fichiers temporaire** : upload sécurisé, lien unique, expiration automatique, protection optionnelle par mot de passe, historique pour les utilisateurs authentifiés.

| Couche | Technologie | Port |
|--------|-------------|------|
| Frontend | Angular 22 (SPA, TypeScript) | **4200** |
| Backend | Spring Boot 3.4 / Java 17 | **8080** |
| Base de données | PostgreSQL | **5432** |
| Fichiers | Dossier local `uploads/` | — |

Swagger UI : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Fonctionnalités (US)

| US | Description |
|----|-------------|
| US01 | Upload authentifié (token unique, expiration 1–7 j) |
| US02 | Téléchargement via lien + métadonnées |
| US03 | Création de compte |
| US04 | Connexion JWT |
| US05 | Historique des fichiers |
| US06 | Suppression (disque + BDD) |
| US07 | Upload anonyme |
| US08 | Tags |
| US09 | Mot de passe fichier (BCrypt) |
| US10 | Expiration automatique + purge planifiée |

---

## Architecture

```
Utilisateur  ↔  Angular (:4200)  ↔  Spring Boot (:8080)  ↔  PostgreSQL (:5432)
                                         ↕
                               Stockage fichiers (uploads/)
```

- **Frontend** : UI, JWT en `localStorage`, upload multipart  
- **Backend** : API REST, sécurité JWT, validation, services métier  
- **PostgreSQL** : métadonnées (users, files)  
- **Disque** : binaires uploadés  

Voir `docs/` pour le détail (architecture, sécurité, tests, perf, maintenance).

---

## Prérequis

- **Java 17+** et Maven 3.9+ (ou `./mvnw`)
- **Node.js 20+** et npm
- **PostgreSQL 14+**
- (Optionnel) Docker / Docker Compose

---

## Installation rapide

### 1. Base de données

```bash
# Option A — script SQL
psql -U postgres -f scripts/init-db.sql

# Option B — Docker Compose
docker compose -f scripts/docker-compose.yml up -d
```

### 2. Backend

```bash
cd datashare_backend
# Adapter src/main/resources/application.yml si besoin
./mvnw spring-boot:run
# API : http://localhost:8080
```

### 3. Frontend

```bash
cd datashare_frontend
npm install
npm start
# App : http://localhost:4200
```

### Compte de test

Créer un compte via `/register`, puis se connecter sur `/login`.

---

## Configuration

Fichier : `datashare_backend/src/main/resources/application.yml`

| Clé | Description | Exemple |
|-----|-------------|---------|
| `spring.datasource.url` | JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/datashare` |
| `spring.datasource.username` | Utilisateur BDD | `postgres` |
| `spring.datasource.password` | Mot de passe BDD | *(à changer en prod)* |
| `security.jwt.secret` | Secret HMAC JWT (≥ 32 car.) | *(secret fort en prod)* |
| `security.jwt.expiration` | Durée JWT (ms) | `3600000` |
| `spring.servlet.multipart.max-file-size` | Taille max upload | `1024MB` |

Variables d’environnement recommandées en production : `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`.

---

## API principale MVP et API optionnelles

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/api/auth/register` | Non | Inscription |
| POST | `/api/auth/login` | Non | Connexion → JWT |
| POST | `/api/files/upload` | JWT | Upload authentifié |
| POST | `/api/files/upload/anonymous` | Non | Upload anonyme |
| GET | `/api/files` | JWT | Liste des fichiers |
| GET | `/api/files/history` | JWT | Historique |
| GET | `/api/files/info/{id}` | JWT | Détail fichier |
| DELETE | `/api/files/info/{id}` | JWT | Suppression |
| PUT | `/api/files/{id}/tags` | JWT | Mise à jour tags |
| GET | `/api/files/metadata/{token}` | Non | Métadonnées avant download |
| POST | `/api/files/download/{token}` | Non* | Téléchargement (*mdp si protégé) |
| POST | `/api/share` | JWT | Partage (notification / lien) |

Documentation interactive : **Swagger** sur le backend.

---

## Tests

```bash
# Backend (JUnit + JaCoCo)
cd datashare_backend && ./mvnw clean test
# Rapport : target/site/jacoco/index.html

# Frontend unitaire
cd datashare_frontend && npm test
npm run test:coverage

# E2E Cypress (frontend démarré)
cd datashare_frontend && npm run e2e:run
```

Détail : [`docs/TESTING.md`](docs/TESTING.md)

---

## Qualité & maintenance

| Document | Contenu |
|----------|---------|
| [docs/TESTING.md](docs/TESTING.md) | Stratégie de tests, couverture, scénarios |
| [docs/SECURITY.md](docs/SECURITY.md) | JWT, BCrypt, IDOR, upload, menaces |
| [docs/PERF.md](docs/PERF.md) | Index SQL, timeouts, monitoring |
| [docs/MAINTENANCE.md](docs/MAINTENANCE.md) | Exploitation, backups, dépannage |

---

## Structure du dépôt

```
datashare/
├── README.md                 ← ce fichier
├── docs/                     ← documentation technique & qualité
├── scripts/                  ← init BDD, docker-compose
├── datashare_backend/        ← Spring Boot
└── datashare_frontend/       ← Angular
```

---

## Licence & formation

Projet réalisé dans le cadre d’une formation (OpenClassrooms).  
Usage pédagogique — adapter secrets et configuration avant toute mise en production.
