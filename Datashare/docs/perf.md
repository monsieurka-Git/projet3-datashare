# Performance — DataShare

## 1. Objectif

1. Ce document fixe les **objectifs de performance du MVP** DataShare.
2. Il décrit les points sensibles, les résultats k6 et les actions prioritaires.
3. Les cibles ci-dessous sont des **objectifs de conception**, pas des SLA contractuels.

Parcours concernés : authentification, liste « Mes fichiers », métadonnées, upload et download (jusqu’à 1 Go).

## 2. Contexte technique

| Couche | Impact principal |
|--------|------------------|
| API Spring Boot | CPU (BCrypt, JSON), disque, JDBC |
| PostgreSQL | Requêtes sur `ownerId`, `downloadToken`, `expiresAt` |
| Stockage `uploads/` | Latence et débit disque |
| Angular | Bundle initial, listes, détection de changements |
| Réseau | Taille des fichiers et latence client ↔ serveur |

## 3. Objectifs indicatifs

| Parcours | Objectif |
|----------|----------|
| Connexion / inscription | < 500 ms (hors réseau très lent) |
| Liste < 100 fichiers | < 300 ms |
| Métadonnées `GET /metadata/{token}` | < 200 ms |
| Upload < 5 Mo | < 2 s |
| Upload proche de 1 Go | Temps lié au débit réseau ; timeout client à documenter |
| Téléchargement | Dépendant surtout du réseau (idéalement en streaming) |

### 3.1 Exemple de test K6 : 

**Configuration :** 10 VUs pendant 30 secondes, avec un arrêt progressif de 30 secondes.

```
k6 run login-list-files.js
```
![Test coverage](images\k6_login-list-files.png)

Autre test K6 :

**Configuration :** 20 VUs pendant 20 secondes.

```
k6 run k6_metadata-download.js
```
**Configuration :** 5 itérations partagées entre 5 VUs.

```
k6 run upload-10mb.js
```
### 3.2 Tests de charge recommandés

| Scénario | Outil | Objectif |
|---|---|---|
| 10 utilisateurs simulés sur connexion et liste de fichiers | k6 ou JMeter | Aucune erreur 5xx |
| 5 téléversements simultanés de 10 Mo | k6 | Téléversements réussis sans saturation disque |
| Requêtes parallèles de métadonnées | k6 | p95 inférieur à 500 ms |


## 4. Points sensibles backend

### 4.1 BCrypt

1. Coût CPU volontaire pour freiner les attaques par force brute.
2. Accepté à l’inscription, à la connexion et pour le mot de passe fichier.
3. Ne jamais journaliser les mots de passe ni hasher inutilement.

### 4.2 Upload multipart

1. Limite : **1 Go** ; écriture disque via `Files.copy`.
2. Le timeout Angular de **30 s** peut être trop court sur un réseau lent.
3. À documenter et ajuster selon le contexte.

### 4.3 Download

1. Le MVP charge encore le fichier en mémoire (`loadFileBytes`).
2. Acceptable pour des fichiers modérés ; risqué près de 1 Go (RAM).
3. **Priorité** : passer à `Resource` / `InputStreamResource` (streaming).

### 4.5 Purge

1. Job nocturne (ex. 03:00) pour limiter l’impact en journée.
2. Suppression via `findByExpiresAtBefore` (disque + base).

## 5. Index PostgreSQL

Les index suivants sont recommandés en production afin d’accélérer les recherches, l’historique utilisateur et la purge :

```sql
CREATE UNIQUE INDEX IF NOT EXISTS idx_files_download_token ON files (download_token);
CREATE INDEX IF NOT EXISTS idx_files_owner_id ON files (owner_id);
CREATE INDEX IF NOT EXISTS idx_files_expires_at ON files (expires_at);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (email);
```

