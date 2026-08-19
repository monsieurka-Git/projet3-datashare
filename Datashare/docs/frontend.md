# Frontend

## Prérequis

- Node.js 20+, npm
- Backend sur `:8080` pour les appels API réels

## Démarrage

```bash
cd datashare_frontend
npm install
npm start
# http://localhost:4200
```

## Organisation 

| Dossier | Contenu |
|---------|---------|
| `pages/` | welcome, login, register, home, upload, download |
| `services/` | `AuthService`, `FileService` |
| `guards/` | `AuthGuard` (route `/home`) |
| `interceptors/` | Ajoute le JWT Bearer |
| `components/` | header, modal |

```
Navigateur → pages → services HTTP → API :8080
                 ↘ AuthGuard / interceptor JWT
```

## Écrans

Welcome (cloud) → Login / Register → Upload → lien → Download → Mes fichiers (suppression).

voir [accessibility](accessibility.md)
