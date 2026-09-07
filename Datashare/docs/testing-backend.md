# Tests backend

## 1. Outils

1. **JUnit 5** et **Mockito** pour les tests unitaires.
2. **JaCoCo** pour mesurer la couverture.

## 2. Commande

```bash
cd datashare_backend
./mvnw clean test
```

1. Rapport : `target/site/jacoco/index.html`.

![Test coverage](images\coverage-backend-jacoco.png)

## 3. Objectifs

1. Suite de tests unitaire verte (environ 60 tests).
2. Couverture d’instructions **≥ 75 %** (seuil défini dans le `pom.xml`).
3. Parcours couverts : authentification, upload, téléchargement, purge.

## 4. Emplacement

1. Sources de test : `src/test/java`.
2. Les services et contrôleurs sont testés avec des dépendances mockées.
