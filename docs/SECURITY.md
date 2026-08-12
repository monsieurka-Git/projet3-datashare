# Sécurité — DataShare

## 1. Objectif

Documenter les mesures de sécurité du MVP DataShare, les menaces prises en compte, et les recommandations pour un déploiement responsable.

DataShare permet le partage de fichiers via **liens à durée limitée**, avec option de **protection par mot de passe**, pour des utilisateurs authentifiés ou anonymes (upload).

---

## 2. Surface d’attaque (synthèse)

| Surface | Risques principaux |
|---------|-------------------|
| API REST | Auth bypass, IDOR, upload malveillant, énumération |
| Liens de download | Fuite de token, partage trop large, lien expiré non purgé |
| Upload | Malware, extensions dangereuses, dépassement de quota disque |
| Frontend | XSS, token JWT en `localStorage`, CSRF (limité en API stateless) |
| Infra | Secrets en clair, HTTP sans TLS, backups non protégés |

---

## 3. Authentification et session (US03, US04)

### 3.1 Comptes utilisateurs

- Inscription : email **unique** + mot de passe
- Mot de passe utilisateur : **minimum 8 caractères** (contrôle front ; à renforcer côté API si besoin)
- Stockage : **BCrypt** (`PasswordEncoder`) — jamais de mot de passe en clair

### 3.2 JWT

- Émis à la connexion (`JwtProvider`)
- Transport : header `Authorization: Bearer <token>`
- Validation : `JwtFilter` (signature + expiration)
- Endpoints publics (sans JWT) :
  - `/api/auth/**`
  - `/api/files/metadata/**`
  - `/api/files/download/**`
  - `/api/files/upload/anonymous`
- Secret HMAC : `security.jwt.secret` — **long, aléatoire, non commité**

### 3.3 Recommandations

- Réduire la durée de vie des JWT en production
- Prévoir refresh token / révocation si le produit évolue
- Préférer `httpOnly` cookie sécurisé à long terme (évolution) ; le MVP utilise `localStorage` (risque XSS)

---

## 4. Autorisation (IDOR / propriété)

| Action | Contrôle |
|--------|----------|
| Liste / historique | Fichiers filtrés par `ownerId` = utilisateur JWT |
| Suppression | `deleteFileForUser` vérifie le propriétaire |
| Partage | Vérifie que le fichier appartient à l’appelant |
| Tags (US08) | Réservés aux utilisateurs connectés propriétaires |
| Download par token | Connaissance du token = secret de capacité (capability URL) |

Les tokens de download doivent rester **non prédictibles** (UUID) — déjà le cas.

---

## 5. Upload de fichiers (US01, US07)

### 5.1 Contrôles implémentés

| Contrôle | Détail |
|----------|--------|
| Taille max | **1 Go** |
| Extensions interdites | `exe, bat, cmd, com, msi, scr, ps1, vbs, js, jar, sh, dll, sys` |
| Nom stocké | Préfixe UUID + nom normalisé (évite écrasement) |
| Anonyme | `ownerId = null` — pas d’accès historique |

### 5.2 Validation côté client

- Alertes UI taille / extension (réduction de charge, **pas** une barrière de sécurité)
- La **référence** reste le serveur (`FileService`)

### 5.3 Recommandations avancées

- Antivirus / scan asynchrone en production
- Liste blanche MIME + vérification du contenu (magic bytes)
- Quota disque par utilisateur
- Rate limiting sur `/upload` et `/upload/anonymous`

---

## 6. Téléchargement et liens (US02, US09, US10)

### 6.1 Token de download

- Unique, stocké en base, servi via `/download/{token}` (front) et API metadata/download
- Métadonnées accessibles **sans** mot de passe (choix US02) — le binaire peut être protégé

### 6.2 Mot de passe fichier (US09)

- Optionnel à l’upload
- Minimum **6 caractères**
- Stocké en **BCrypt** (`downloadPasswordHash`)
- Vérifié uniquement au téléchargement effectif
- **Pas de récupération** de mot de passe (conforme au besoin)

### 6.3 Expiration (US10)

- Durée 1–7 jours (défaut 7)
- Liens expirés refusés (`410` / message métier)
- Purge planifiée quotidienne (fichier + métadonnées)

### 6.4 Bonnes pratiques d’usage

- Ne pas publier les liens sur des canaux publics non maîtrisés
- Activer un mot de passe fichier pour les contenus sensibles
- Choisir la durée d’expiration la plus courte possible

---

## 7. API et HTTP

### 7.1 Spring Security

- Session **stateless**
- CSRF désactivé (API JWT — classique pour SPA + Bearer)
- CORS : origines front restreintes (dev : `localhost:4200`)

### 7.2 Headers recommandés en production (reverse-proxy)

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'; ...
Referrer-Policy: no-referrer
```

### 7.3 Gestion des erreurs

- `GlobalExceptionHandler` : messages métier sans stack trace client
- Ne pas exposer de chemins disque ni détails SQL

---

## 8. Frontend

| Sujet | Mesure / risque |
|-------|------------------|
| JWT en `localStorage` | Vulnérable à XSS — minimiser `innerHTML`, dépendances sûres |
| Interceptor | N’envoie pas le JWT sur metadata/download/anonymous |
| AuthGuard | Protège `/home` (routes authentifiées) |
| Validation formulaires | UX + première barrière (email, longueur mdp) |

---

## 9. Données personnelles et conformité (indicative)

- Email utilisateur stocké pour le compte
- Fichiers potentiellement personnels : durée de rétention limitée par design (expiration)
- Logs : ne pas journaliser mots de passe, contenus de fichiers, JWT complets
- Pour un déploiement réel en UE : analyser le besoin **RGPD** (base légale, DPA hébergeur, durée de conservation)

---

## 10. Secrets et configuration

| Secret | Règle |
|--------|-------|
| `security.jwt.secret` | Aléatoire ≥ 32 octets, rotatif si fuite |
| Mot de passe BDD | Fort, dédié, droits minimaux |
| Clés CI/CD | Hors dépôt Git |

Checklist :

- [ ] Aucun secret dans le dépôt
- [ ] HTTPS uniquement en production
- [ ] `ddl-auto` non destructif en prod (`validate` + migrations)
- [ ] Backups chiffrés / accès restreint

---

## 11. Journalisation sécurité

Événements utiles à conserver (côté serveur) :

- Échecs de login répétés (à corréler — rate limit souhaitable)
- Upload refusé (extension / taille)
- Accès download refusé (token invalide, expiré, mauvais mot de passe)
- Suppressions de fichiers
- Exécution de la purge US10

---

## 12. Tests liés à la sécurité

Voir aussi `TESTING.md` :

- Rejet extension / taille (unitaires `FileService`)
- Mot de passe fichier incorrect / manquant (`FileDownloadService`)
- Suppression interdite pour non-propriétaire
- Endpoints publics vs authentifiés (`JwtFilter`, SecurityConfig)
- Scénarios E2E : parcours auth, upload, delete (non exhaustifs pour la sécu offensive)

Tests additionnels recommandés avant prod : scan dépendances (`npm audit`, OWASP Dependency-Check), header scan, test d’IDOR manuel.

![alt text](Audit_Frontend.md)

---

## 13. Matrice des menaces (résumé)

| Menace | Gravité MVP | Mitigation actuelle | Suite possible |
|--------|-------------|---------------------|----------------|
| Vol de JWT (XSS) | Élevée | Bonnes pratiques Angular | Cookies httpOnly + CSP stricte |
| Lien de download fuité | Moyenne | Expiration + mdp optionnel | OTP, one-time link |
| Upload malware | Élevée | Liste noire extensions | Antivirus, allowlist MIME |
| IDOR fichiers | Élevée | Contrôle `ownerId` | Tests automatisés IDOR |
| Abus upload anonyme | Moyenne | Limites taille/type | Captcha, rate limit IP |
| Secret JWT faible | Critique | Config externalisée | Rotation, secrets manager |

---

## 14. Conclusion

Le MVP DataShare applique des **contrôles de base solides** : BCrypt, JWT, tokens de download non prédictibles, validation d’upload, expiration et purge, contrôle de propriété.

Avant une exposition Internet large, traiter en priorité : **HTTPS**, **secrets**, **rate limiting**, **streaming/scan antivirus**, et **réduction du risque XSS** autour du stockage du JWT.
