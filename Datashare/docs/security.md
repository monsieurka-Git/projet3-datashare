# Sécurité — DataShare

## 1. Objectif

	.Décrire les mesures de sécurité du MVP DataShare.
	.Rappeler les risques principaux et les contrôles en place.
	.Lister les priorités avant une exposition Internet large.

## 2. Surface d’attaque

| Surface | Risques principaux |
|---------|-------------------|
| API REST | Contournement d’auth, IDOR, upload abusif |
| Liens de download | Token fuité, lien trop largement partagé |
| Upload | Fichier dangereux, saturation disque |
| Frontend | XSS, JWT en `localStorage` |
| Infra | Secrets en clair, HTTP sans TLS |

## 3. Authentification (US03, US04)

1. Compte : email **unique** + mot de passe (minimum **8 caractères** côté UI).
2. Mot de passe stocké avec **BCrypt** (jamais en clair).
3. Connexion : émission d’un **JWT** (`Authorization: Bearer …`).
4. Endpoints publics (sans JWT) :
   - `/api/auth/**`
   - `/api/files/metadata/**`
   - `/api/files/download/**`
   - `/api/files/upload/anonymous`
5. Secret HMAC (`security.jwt.secret`) : long, aléatoire, **non versionné**.

## 4. Autorisation

1. Liste et historique filtrés par `ownerId` (utilisateur du JWT).
2. Suppression et tags réservés au **propriétaire**.
3. Download : connaissance du **token UUID** (lien de capacité).
4. Token de download non prédictible.

## 5. Upload (US01, US07)

1. Taille max : **1 Go**.
2. Extensions interdites (ex. `exe`, `bat`, `js`, `jar`, `sh`, `dll`…).
3. Nom stocké préfixé d’un UUID (évite les collisions).
4. Upload anonyme : `ownerId = null`, pas d’historique.
5. La validation **serveur** fait foi ; le contrôle client n’est qu’une aide UX.

## 6. Téléchargement, mot de passe fichier, expiration

1. Métadonnées accessibles via le token ; le binaire peut exiger un mot de passe.
2. Mot de passe fichier (US09) : optionnel, min. **6 caractères**, stocké en **BCrypt**.
3. Pas de récupération de mot de passe fichier.
4. Expiration 1–7 jours (défaut 7) ; lien expiré refusé.
5. Purge planifiée : fichier disque + ligne BDD (US10).
6. Download API : **POST** `/api/files/download/{token}` (aligné avec le code).

## 7. API et HTTP

1. Session **stateless** (JWT).
2. CORS limité aux origines front connues (dev : `localhost:4200`).
3. Erreurs métier sans stack trace ni chemin disque côté client.
4. En production : **HTTPS** + en-têtes de sécurité (HSTS, `X-Content-Type-Options`, etc.).

## 8. Frontend

1. JWT en `localStorage` : risque XSS (choix MVP).
2. Mitigations : échappement Angular, logout qui efface le token, JWT à durée limitée.
3. Interceptor : pas de Bearer sur metadata / download / upload anonyme.
4. AuthGuard sur les routes connectées (`/home`).

## 9. Secrets et configuration

| Élément | Règle |
|---------|--------|
| `JWT_SECRET` | ≥ 32 caractères, via variable d’environnement |
| Mot de passe BDD | Fort, droits minimaux |
| Dépôt Git | Aucun secret de production |

Checklist : HTTPS, `ddl-auto` non destructif en prod, backups protégés.

## 10. Journalisation utile

1. Échecs de login répétés.
2. Upload refusé (taille / extension).
3. Download refusé (token, expiration, mauvais mot de passe).
4. Suppressions et exécution de la purge.
5. Ne jamais logger mots de passe, JWT complets ou contenu des fichiers.

## 11. Synthèse menaces

| Menace | Mitigation actuelle | Suite possible |
|--------|--------------------|----------------|
| Vol de JWT (XSS) | Bonnes pratiques Angular | Cookie `httpOnly`, CSP |
| Lien fuité | Expiration + mdp optionnel | Lien à usage unique |
| Upload malveillant | Liste noire d’extensions | Antivirus, allowlist MIME |
| IDOR | Contrôle `ownerId` | Tests automatisés dédiés |
| Abus anonyme | Limites taille / type | Rate limit, captcha |

## 12. Sécurité GitHub
- Accès contrôlé (2FA recommandé, rôles Read/Write/Admin).
- Branches protégées : pas de push direct, PR + review obligatoires.
- Analyse du code (CodeQL) pour détecter failles et injections.
- Dependabot : mises à jour automatiques des dépendances vulnérables.
- Secrets chiffrés dans GitHub Secrets pour les workflows CI/CD.


## 13. Conclusion

1. Bases solides : BCrypt, JWT, tokens UUID, validation upload, expiration, propriété.
2. Avant production large : HTTPS, secrets, rate limiting, streaming / scan, réduction du risque XSS autour du JWT.