# DataShare

Application web de **partage de fichiers temporaire** : upload (authentifié ou anonyme), lien unique à durée limitée, protection optionnelle par mot de passe, historique et suppression pour les utilisateurs connectés.

## Stack

| Couche | Technologie |
|--------|-------------|
| Frontend | Angular 22 (standalone), TypeScript |
| Backend | Spring Boot 3.4, Java 17+ |
| Sécurité | Spring Security, JWT, BCrypt |
| Base de données | PostgreSQL 14+ |
| Stockage fichiers | Disque local (`uploads/`) |
| Tests | JUnit 5 + Mockito + JaCoCo · Vitest · Cypress |

## Prérequis

- JDK 17+
- Maven 3.9+ (ou `./mvnw`)
- Node.js 20+ et npm
- PostgreSQL 14+  
  **ou** Docker (recommandé pour la BDD)

## Installation rapide

### 1. Base de données

**Docker Compose** 

```bash
cd datashare_backend
docker compose up -d db
# PostgreSQL : localhost:5432 / user: postgres / password: postgres / db: datashare
```

### 2. Backend

```bash
cd datashare_backend
# Vérifier src/main/resources/application.yml (URL, user, password, secret JWT)
./mvnw spring-boot:run
# API : http://localhost:8080
# Swagger : http://localhost:8080/swagger-ui.html (si OpenAPI actif)
```

### 3. Frontend

### 2.Installation de Node.js
Version recommandée : Node 18+

### 3.Installation de l’Angular CLI
```bash
npm install -g @angular/cli
```
Vérification :

```bash
ng version
```
### 4.Création du projet Angular DataShare
Le frontend a été initialisé via Angular CLI avec la commande :

```bash
ng new datashare-frontend
```

```bash
cd datashare_frontend
npm install
npm start
# App : http://localhost:4200
```

## Utilisation

1. Ouvrir http://localhost:4200 → page d’accueil
2. **Créer un compte** / **Se connecter**
3. **+ Ajouter des fichiers** : téléverser (expiration 1–7 j, mdp optionnel, tags)
4. Copier le lien de téléchargement ou consulter **Mes fichiers**
5. Accès public au lien : `/download/{token}` (métadonnées puis téléchargement)

Upload **anonyme** possible depuis la page welcome (sans historique « Mes fichiers »).

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
# Backend (JaCoCo ≥ 75 % hors configs Spring)
cd datashare_backend && ./mvnw clean test

# Frontend unitaire
cd datashare_frontend && npm test && npm run test:coverage

# E2E Cypress (frontend démarré sur :4200)
cd datashare_frontend && npm run e2e:run
```

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
├── datashare_backend/        ← Spring Boot
└── datashare_frontend/       ← Angular
```

---

## Licence & formation

Projet réalisé dans le cadre d’une formation (OpenClassrooms).  
Usage pédagogique — adapter secrets et configuration avant toute mise en production.
