# Backend

## 1. Prérequis

1. Java 17 ou supérieur.
2. Maven (`./mvnw`).
3. PostgreSQL (Docker Compose recommandé).

## 2. Démarrage

```bash
cd datashare_backend
./mvnw spring-boot:run
```

1. API sur le port **8080**.
2. Documentation Swagger (lien actif si frontend started) : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## 3. Secrets

1. En production, les secrets ne sont pas versionnés en clair.
2. Variables : `JWT_SECRET`, `DB_PASSWORD` (voir fichier [env.example](environnement.md))

```powershell
$env:JWT_SECRET="un_secret_d_au_moins_32_caracteres"
$env:DB_PASSWORD="postgres"
./mvnw spring-boot:run
```

Pour la gestion sécurisée des secrets, nous utilisons des variables d’environnement en local et des secrets GitHub Actions pour la CI/CD.
En production, un Secret Manager (Vault ou celui du cloud) permettrait de centraliser, chiffrer et gérer la rotation des secrets. »

<u>Exemples :</u> HashiCorp Vault, AWS Secrets Manager, Azure Key Vault, Google Secret Manager..

## 4. Organisation du code

| Couche | Rôle |
|--------|------|
| `controller` | Endpoints HTTP |
| `service` | Règles métier (upload, download, auth…) |
| `repository` | Accès PostgreSQL (JPA) |
| `scheduler` | Purge des fichiers expirés |

## 5. Endpoints principaux

| Endpoint | Pour qui | Utilité |
|----------|----------|---------|
| `POST /api/auth/register` | Public | Créer un compte |
| `POST /api/auth/login` | Public | Obtenir un JWT |
| `POST /api/files/upload` | Connecté | Envoyer un fichier |
| `POST /api/files/upload/anonymous` | Anonyme | Envoyer sans compte |
| `GET /api/files` | Connecté | Liste « Mes fichiers » |
| `GET /api/files/metadata/{token}` | Public | Infos avant téléchargement |
| `POST /api/files/download/{token}` | Public | Télécharger le fichier |
| `DELETE /api/files/info/{id}` | Propriétaire | Supprimer un fichier |

## 6. Règles métier côté API

1. Validation de la taille et des extensions à l’upload.
2. Token de téléchargement unique (UUID).
3. Contrôle d’expiration avant tout download.
4. Mot de passe fichier stocké hashé (BCrypt) si renseigné.
