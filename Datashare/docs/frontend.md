# Frontend

## 1. Prérequis

1. Node.js 20+ et npm.
2. Backend démarré sur `:8080` pour les appels réels.

## 2. Démarrage

```bash
cd datashare_frontend
npm install
npm start
```

1. Application disponible (lien actif si frontend started) : [http://localhost:4200](http://localhost:4200).

## 3. Organisation

| Élément | Contenu |
|---------|---------|
| `pages/` | welcome, login, register, upload, home, download |
| `services/` | AuthService, FileService |
| `guards/` | AuthGuard (protection de `/home`) |
| `interceptors/` | Injection du JWT Bearer |
| `components/` | en-tête, modales |

## 4. Parcours principaux

1. **Welcome** → entrée dans l’application.
2. **Login / Register** → authentification.
3. **Upload** → dépôt du fichier et obtention du lien.
4. **Home** → historique et suppression.
5. **Download** → page publique du lien partagé.

## 5. User Experience (UX) et gestion d’erreurs

1. Messages d’erreur affichés en texte (login, upload, download).
2. Bouton **Se déconnecter** visible sur les pages connectées.
3. Confirmation avant suppression d’un fichier.
4. Labels et attributs ARIA sur les éléments principaux (voir Accessibilité).
