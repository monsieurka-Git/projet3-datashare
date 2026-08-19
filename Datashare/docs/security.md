# Sécurité

## Secrets externalisés

```yaml
# application.yml
spring.datasource.password: ${DB_PASSWORD}
security.jwt.secret: ${JWT_SECRET}
```

## Auth

- Mots de passe **BCrypt**
- **JWT** (Bearer), expiration ~1 h
- Contrôle propriétaire (`ownerId`) sur liste / suppression / tags

## JWT dans localStorage

| Risque | Mitigation |
|--------|------------|
| XSS | Angular échappe les templates ; pas d’HTML utilisateur brut |
| Session longue | JWT à durée limitée |
| Fuite | Interceptor n’envoie pas le token sur download / metadata / upload anonyme |
| Fin de session | `logout()` efface token + userId |

Évolution : cookie `httpOnly` + HTTPS strict.

## Upload

Taille max 1 Go · extensions dangereuses refusées côté serveur.

## Audit dépendances

```bash
cd datashare_frontend && npm audit
```
```bash
cd datashare_frontend && npm audit fix
```
