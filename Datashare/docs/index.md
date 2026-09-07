# Accueil

## 1. Présentation de DataShare

1. DataShare est une application de **partage de fichiers temporaire**.
2. L’émetteur obtient un **lien unique** après l’envoi d’un fichier.
3. Le destinataire télécharge le fichier **sans créer de compte**.
4. Options disponibles : expiration (1 à 7 jours), mot de passe, tags, upload anonyme.

## 2. Choix de la stack (justification)

### 2.1 Frontend — Angular

1. Framework structurant pour une SPA (routing, services, guards).
2. Typage **TypeScript** : moins d’erreurs et code plus maintenable.
3. Interceptor JWT et AuthGuard adaptés à l’API Spring.
4. Aligné avec le cadre de formation full-stack.

### 2.2 Backend — Spring Boot 3 / Java 17

1. Standard pour les API REST sécurisées en Java.
2. Spring Security + JWT pour l’authentification sans session serveur.
3. JPA / Hibernate pour les métadonnées en PostgreSQL.
4. Tâche planifiée (`@Scheduled`) pour la purge des fichiers expirés.

### 2.3 Base de données — PostgreSQL

1. Base relationnelle fiable (contraintes, transactions).
2. Adaptée au modèle User → Files (email unique, token unique).
3. Compatible Docker Compose pour un démarrage simple en local.

### 2.4 Synthèse des ports

| Couche | Technologie | Port |
|--------|-------------|------|
| Frontend | Angular | **4200** |
| Backend | Spring Boot 3 / Java 17 | **8080** |
| Base de données | PostgreSQL | **5432** |
| Fichiers | dossier `uploads/` | — |

## 3. Schéma Architecture

![Schema archi](images\schema_architecture_datashare.png)


## 4. Un flux métier — téléchargement

1. Le destinataire ouvre le lien `/download/{token}`.
2. Le front appelle **GET** `/api/files/metadata/{token}`.
3. Puis **POST** `/api/files/download/{token}` pour récupérer le fichier.
4. Si le fichier est protégé, le mot de passe est demandé à l’écran.

!!! note "Alignement doc et code"
    Le téléchargement du binaire se fait en **POST**, pas en GET.

## 5. Démarrage express

```bash
docker compose up -d db
cd datashare_backend && ./mvnw spring-boot:run
cd datashare_frontend && npm install && npm start
```

1. Front (lien actif si frontend started) : [http://localhost:4200](http://localhost:4200).
2. Swagger (lien actif si backend started) : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## 6. Navigation dans la documentation

1. [Backend](backend.md) — API, couches, endpoints.
2. [Frontend](frontend.md) — pages, guards, services.
3. [Guide utilisateur](guide-utilisateur.md) — parcours d’usage.
4. [Tests backend](testing-backend.md) / [Tests frontend](testing-frontend.md).
5. [Performance](perf.md), [Sécurité](security.md), [Maintenance](maintenance.md), [Accessibilité](accessibility.md).
