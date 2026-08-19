# Backend

## Prérequis

- Java 17+, Maven (`./mvnw`)
- PostgreSQL (ou `docker compose up -d`db)

## Secrets

Plus de secrets en clair versionnés pour la prod.

`Variables d’environnement :`

```bash
# Windows PowerShell
$env:JWT_SECRET="un_secret_aleatoire_d_au_moins_32_caracteres"
$env:DB_PASSWORD="postgres"
./mvnw spring-boot:run
```

Voir variable [environnement](environnement.md)


## Démarrage

```bash
cd datashare_backend
./mvnw spring-boot:run
# API :8080 · Swagger : /swagger-ui.html
```

## Organisation du code

| Couche | Rôle |
|--------|------|
| `controller` | HTTP (auth, files, download, share) |
| `service` | Règles métier (upload, purge, tags…) |
| `repository` | Accès PostgreSQL (JPA) |
| `scheduler` | Purge horaire des fichiers expirés |

## Endpoints — à quoi ça sert, pour qui

| Endpoint | Pour qui | À quoi ça sert |
|----------|----------|----------------|
| `POST /api/auth/register` | Public | Créer un compte |
| `POST /api/auth/login` | Public | Obtenir un JWT |
| `POST /api/files/upload` | Connecté | Envoyer un fichier + options |
| `POST /api/files/upload/anonymous` | Anonyme | Envoyer sans compte |
| `GET /api/files` | Connecté | Voir « Mes fichiers » |
| `GET /api/files/metadata/{token}` | Public | Infos avant téléchargement |
| **`POST /api/files/download/{token}`** | Public | **Télécharger le fichier** |
| `DELETE /api/files/info/{id}` | Propriétaire | Supprimer |
| `PUT /api/files/{id}/tags` | Propriétaire | Organiser par tags |
| `POST /api/share` | Connecté | Partager un lien |


