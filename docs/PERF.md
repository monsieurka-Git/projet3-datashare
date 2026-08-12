# Performance — DataShare

## 1. Objectifs

Garantir des temps de réponse acceptables pour :

- l’authentification et la navigation ;
- l’upload / download de fichiers jusqu’à **1 Go** ;
- la liste « Mes fichiers » pour un usage individuel / petite équipe (MVP).

Ce document décrit les goulots d’étranglement identifiés, les bonnes pratiques et les pistes d’évolution.

---

## 2. Contexte technique

| Couche | Impact perf |
|--------|-------------|
| API Spring Boot | CPU (BCrypt, JSON), I/O disque, JDBC |
| PostgreSQL | Requêtes par `ownerId`, `downloadToken`, `expiresAt` |
| Stockage `uploads/` | I/O disque local (pas d’object storage au MVP) |
| Angular | Bundle initial, change detection, listes |
| Réseau | Taille des uploads/downloads, latence |

---

## 3. Exigences cibles (MVP)

| Parcours | Cible indicative |
|----------|------------------|
| Login / register | < 500 ms (hors latence réseau élevée) |
| Liste des fichiers (< 100 fichiers) | < 300 ms |
| Métadonnées download (`GET /metadata/{token}`) | < 200 ms |
| Upload petit fichier (< 5 Mo) | < 2 s |
| Upload large (approche 1 Go) | linéaire selon débit ; timeout client 30 s configurable |
| Download | Streaming / bytes ; dépend du réseau |

> Ces chiffres sont des **objectifs de conception MVP**, pas des SLA contractuels.

---

## 4. Backend

### 4.1 Points sensibles

1. **BCrypt** à l’inscription / login / hash mot de passe fichier  
   - Coût CPU volontaire (sécurité)  
   - Ne pas logger ni re-hasher inutilement

2. **Upload multipart**  
   - Taille max 1 Go (`FileService`)  
   - Écriture disque via `Files.copy`  
   - Timeout HTTP client Angular : 30 s (à augmenter si gros fichiers sur réseau lent)

3. **Download**  
   - Chargement via `loadFileBytes` (fichier entier en mémoire)  
   - **Limite MVP** : pour de très gros fichiers, préférer à terme un stream (`Resource` / `InputStreamResource`) pour limiter la RAM

4. **Purge US10**  
   - Job nocturne (03:00) : évite la charge en journée  
   - Boucle simple sur `findByExpiresAtBefore`

### 4.2 Index BDD recommandés

Vérifier / créer en production :

```sql
-- Token de téléchargement (US02) — déjà unique en JPA si contrainte présente
CREATE UNIQUE INDEX IF NOT EXISTS idx_files_download_token ON files (download_token);

-- Historique utilisateur (US05)
CREATE INDEX IF NOT EXISTS idx_files_owner_id ON files (owner_id);

-- Purge (US10)
CREATE INDEX IF NOT EXISTS idx_files_expires_at ON files (expires_at);

-- Email unique utilisateurs (US03)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (email);
```

### 4.3 Configuration JVM / serveur (prod)

- Dimensionner le heap selon la taille max des fichiers **si** le download reste entièrement en mémoire
- Préférer le **streaming** avant d’augmenter indéfiniment le heap
- Tomcat / threads : adapter `server.tomcat.threads.max` au trafic attendu
- Désactiver le détail des erreurs en production

### 4.4 Pistes d’évolution

| Amélioration | Bénéfice |
|--------------|----------|
| `InputStreamResource` pour le download | Moins de RAM |
| Object storage (S3, MinIO) | Scalabilité disque |
| Pagination `GET /api/files` | Listes volumineuses |
| Cache métadonnées token (Caffeine/Redis) | Lecture metadata très fréquente |
| CDN pour le front | TTFB assets |

---

## 5. Frontend

### 5.1 Points d’attention

- **Change detection** : forcer `detectChanges()` uniquement aux points async critiques (upload/download) déjà identifiés
- **Listes** : « Mes fichiers » charge l’ensemble des fichiers de l’utilisateur — pagination UI si > 100–200 entrées
- **Bundle Angular** : budgets dans `angular.json` (warning 500 kB / error 1 MB initial)
- **Images / assets** : pas de grosses images non optimisées dans `public/`

### 5.2 Upload UX

- Validation **taille / extension** côté client avant envoi (évite les allers-retours inutiles)
- Message d’état « Téléversement en cours… » + désactivation du bouton (anti double-submit)
- Timeout 30 s : documenter pour les gros fichiers / réseaux lents

### 5.3 Mesure côté navigateur

- Chrome DevTools → Network (waterfall, poids)
- Lighthouse (perf front, perf uniquement sur pages publiques)
- Onglet Performance pour les longs scripts

---

## 6. Réseau et CORS

- Préflight `OPTIONS` autorisé (SecurityConfig) — coût négligeable si bien mis en cache navigateur
- En production : **même site** ou reverse-proxy (`/api` → backend) pour réduire la complexité CORS
- HTTPS : léger surcoût CPU, obligatoire en prod

---

## 7. Scénarios de charge (indicatif)

| Scénario | Outil possible | Objectif |
|----------|----------------|----------|
| 10 users login + liste fichiers | k6 / JMeter | Pas d’erreur 5xx |
| 5 uploads simultanés 10 Mo | k6 | Succès, pas de saturation disque |
| Download parallèle metadata | k6 | p95 < 500 ms |

Le MVP n’inclut pas de campagne de charge formalisée ; ces scénarios sont recommandés avant une mise en production élargie.

PS D:\Formation_openclassrooms\datashare-fix-login-error-v24\datashare_backend\k6> k6 run login-list-files.js

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 


     execution: local
        script: login-list-files.js
        output: -

     scenarios: (100.00%) 1 scenario, 10 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 10 looping VUs for 30s (gracefulStop: 30s)



  █ TOTAL RESULTS 

    checks_total.......: 876    28.345958/s
    checks_succeeded...: 33.33% 292 out of 876
    checks_failed......: 66.66% 584 out of 876

    ✗ login status is 200
      ↳  0% — ✓ 0 / ✗ 292
    ✗ files status is 200
      ↳  0% — ✓ 0 / ✗ 292
    ✓ no server error

    HTTP
    http_req_duration....: avg=16.19ms min=1.05ms med=5.61ms max=444.46ms p(90)=21.67ms p(95)=31.92ms
    http_req_failed......: 100.00% 584 out of 584
    http_reqs............: 584     18.897305/s

    EXECUTION
    iteration_duration...: avg=1.03s   min=1s     med=1.01s  max=1.48s    p(90)=1.04s   p(95)=1.06s  
    iterations...........: 292     9.448653/s
    vus..................: 10      min=10         max=10
    vus_max..............: 10      min=10         max=10

    NETWORK
    data_received........: 239 kB  7.7 kB/s
    data_sent............: 90 kB   2.9 kB/s




running (0m30.9s), 00/10 VUs, 292 complete and 0 interrupted iterations
default ✓ [======================================] 10 VUs  30s
PS D:\Formation_openclassrooms\datashare-fix-login-error-v24\datashare_backend\k6> k6 run metadata-download.js

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 


     execution: local
        script: metadata-download.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 50s max duration (incl. graceful stop):
              * default: 20 looping VUs for 20s (gracefulStop: 30s)



  █ THRESHOLDS 

    http_req_duration
    ✓ 'p(95)<500' p(95)=22.02ms


  █ TOTAL RESULTS 

    checks_total.......: 62088  3102.370631/s
    checks_succeeded...: 50.00% 31044 out of 62088
    checks_failed......: 50.00% 31044 out of 62088

    ✗ metadata status is 200
      ↳  0% — ✓ 0 / ✗ 31044
    ✓ no server error

    HTTP
    http_req_duration....: avg=12.53ms min=1.53ms med=10.22ms max=402.72ms p(90)=17.2ms  p(95)=22.02ms
    http_req_failed......: 100.00% 31044 out of 31044
    http_reqs............: 31044   1551.185315/s

    EXECUTION
    iteration_duration...: avg=12.85ms min=1.82ms med=10.49ms max=430.59ms p(90)=17.62ms p(95)=22.6ms 
    iterations...........: 31044   1551.185315/s
    vus..................: 20      min=20             max=20
    vus_max..............: 20      min=20             max=20

    NETWORK
    data_received........: 15 MB   727 kB/s
    data_sent............: 3.9 MB  194 kB/s




running (20.0s), 00/20 VUs, 31044 complete and 0 interrupted iterations
default ✓ [======================================] 20 VUs  20s

PS D:\Formation_openclassrooms\datashare-fix-login-error-v24\datashare_backend\k6> k6 run upload-10mb.js      

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 


     execution: local
        script: upload-10mb.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 10m30s max duration (incl. graceful stop):
              * default: 5 iterations shared among 5 VUs (maxDuration: 10m0s, gracefulStop: 30s)



  █ TOTAL RESULTS 

    checks_total.......: 10     109.881295/s
    checks_succeeded...: 50.00% 5 out of 10
    checks_failed......: 50.00% 5 out of 10

    ✗ upload status is 200
      ↳  0% — ✓ 0 / ✗ 5
    ✓ no server error

    HTTP
    http_req_duration....: avg=0s   min=0s   med=0s   max=0s   p(90)=0s   p(95)=0s  
    http_req_failed......: 100.00% 5 out of 5
    http_reqs............: 5       54.940648/s

    EXECUTION
    iteration_duration...: avg=91ms min=91ms med=91ms max=91ms p(90)=91ms p(95)=91ms
    iterations...........: 5       54.940648/s

    NETWORK
    data_received........: 1.8 kB  19 kB/s
    data_sent............: 1.5 MB  17 MB/s




running (00m00.1s), 0/5 VUs, 5 complete and 0 interrupted iterations
default ✓ [======================================] 5 VUs  00m00.1s/10m0s  5/5 shared iters
---

## 8. Monitoring recommandé

| Métrique | Pourquoi |
|----------|----------|
| Latence p95 des endpoints `/api/files/**` | Détecter régressions |
| Taux d’erreur 4xx/5xx | Qualité |
| Espace disque `uploads/` | Éviter disk full |
| Durée du job de purge | US10 |
| CPU pendant uploads massifs | Dimensionnement |

---

## 9. Checklist perf avant release

- [ ] Index BDD présents sur `download_token`, `owner_id`, `expires_at`
- [ ] Pas de log verbeux en production
- [ ] Build front en mode production (`npm run build`)
- [ ] Timeouts client cohérents avec la taille max autorisée
- [ ] Job de purge actif et journalisé
- [ ] Espace disque surveillé

---

## 10. Synthèse

DataShare MVP privilégie la **simplicité** (disque local, chargement fichier en mémoire). Les principaux leviers immédiats sont : **index SQL**, **validation client**, **purge nocturne**, et à moyen terme le **streaming des downloads** et un **stockage objet**.
