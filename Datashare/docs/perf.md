# Performance — DataShare

## 1. Objectif et périmètre

Ce document résume les objectifs de performance du MVP **DataShare**, les principaux points sensibles de l’architecture, les résultats des tests k6 et les actions recommandées avant une mise en production élargie.

Les parcours étudiés sont :

- authentification et navigation ;
- liste « Mes fichiers » ;
- consultation des métadonnées d’un partage ;
- téléversement et téléchargement de fichiers, jusqu’à 1 Go.

Les valeurs cibles présentées ci-dessous sont des **objectifs de conception pour le MVP** et ne constituent pas des SLA contractuels.

## 2. Contexte technique

| Couche | Principaux impacts sur les performances |
|---|---|
| API Spring Boot | CPU pour BCrypt et JSON, accès disque et requêtes JDBC |
| PostgreSQL | Requêtes sur `ownerId`, `downloadToken` et `expiresAt` |
| Stockage local `uploads/` | Débit et latence des entrées/sorties disque |
| Angular | Taille du bundle initial, détection des changements et affichage des listes |
| Réseau | Taille des fichiers transférés et latence entre client et serveur |

## 3. Objectifs de performance

| Parcours | Objectif indicatif |
|---|---:|
| Connexion / inscription | < 500 ms, hors latence réseau élevée |
| Liste de fichiers de moins de 100 éléments | < 300 ms |
| Métadonnées d’un téléchargement | < 200 ms pour `GET /metadata/{token}` |
| Téléversement d’un petit fichier de moins de 5 Mo | < 2 s |
| Téléversement proche de 1 Go | Temps linéaire selon le débit réseau ; timeout client configurable |
| Téléchargement | Streaming ou transfert de bytes, dépendant principalement du réseau |

## 4. Points sensibles du backend

### 4.1 Authentification et BCrypt

BCrypt consomme volontairement des ressources CPU afin de ralentir les attaques par force brute. Son coût doit être accepté lors de l’inscription, de la connexion et du hachage d’un mot de passe de fichier.

Il est important de ne pas journaliser les mots de passe et d’éviter tout hachage inutile ou répétitif.

### 4.2 Téléversement multipart

Le téléversement est limité à 1 Go et repose sur une écriture sur disque avec `Files.copy`. Le timeout Angular de 30 secondes peut être insuffisant pour un gros fichier transmis sur un réseau lent ; il doit donc être documenté et ajusté selon le contexte.

### 4.3 Téléchargement

Dans la version actuelle du MVP, le téléchargement utilise `loadFileBytes`, ce qui peut charger l’intégralité du fichier en mémoire. Cette approche est acceptable pour des fichiers modérés, mais elle augmente la consommation RAM pour les fichiers proches de 1 Go.

**Évolution prioritaire :** utiliser `Resource` ou `InputStreamResource` afin de diffuser le fichier progressivement et de limiter l’utilisation mémoire.

### 4.4 Purge des fichiers expirés

Le job de purge est exécuté durant la nuit, à 03:00, afin d’éviter une charge supplémentaire pendant les heures d’utilisation. Il parcourt les fichiers retournés par `findByExpiresAtBefore` et supprime les ressources expirées.

## 5. Indexation PostgreSQL

Les index suivants sont recommandés en production afin d’accélérer les recherches, l’historique utilisateur et la purge :

```sql
-- Token de téléchargement : doit être unique
CREATE UNIQUE INDEX IF NOT EXISTS idx_files_download_token
ON files (download_token);

-- Historique des fichiers par utilisateur
CREATE INDEX IF NOT EXISTS idx_files_owner_id
ON files (owner_id);

-- Recherche des fichiers expirés
CREATE INDEX IF NOT EXISTS idx_files_expires_at
ON files (expires_at);

-- Adresse e-mail unique
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email
ON users (email);
```

## 6. Frontend et expérience utilisateur

Les contrôles suivants limitent les temps d’attente et les actions inutiles :

- appeler `detectChanges()` uniquement aux étapes asynchrones nécessaires, notamment pendant les téléversements et téléchargements ;
- prévoir une pagination de « Mes fichiers » au-delà de 100 à 200 éléments ;
- configurer les budgets Angular avec un avertissement à 500 kB et une erreur à 1 MB pour le bundle initial ;
- éviter les images volumineuses ou non optimisées dans `public/` ;
- valider la taille et l’extension côté client avant l’envoi ;
- afficher « Téléversement en cours… » et désactiver le bouton pour éviter les doubles soumissions ;
- documenter le timeout de 30 secondes pour les fichiers volumineux et les réseaux lents.

### Mesure côté navigateur

Les outils suivants peuvent être utilisés :

- **Chrome DevTools — Network** : waterfall, poids des ressources et durée des requêtes ;
- **Lighthouse** : performance du frontend, notamment sur les pages publiques ;
- **Onglet Performance** : détection des scripts longs et des blocages de l’interface.

## 7. Réseau et CORS

Le préflight `OPTIONS` est autorisé dans `SecurityConfig`. Son coût reste limité lorsque la réponse est correctement mise en cache par le navigateur.

En production, il est préférable d’utiliser un même site ou un reverse proxy, par exemple `/api` redirigé vers le backend, afin de réduire la complexité de la configuration CORS. HTTPS ajoute un léger coût CPU, mais reste obligatoire pour protéger les échanges.

## 8. Tests de charge recommandés

| Scénario | Outil | Objectif |
|---|---|---|
| 10 utilisateurs simulés sur connexion et liste de fichiers | k6 ou JMeter | Aucune erreur 5xx |
| 5 téléversements simultanés de 10 Mo | k6 | Téléversements réussis sans saturation disque |
| Requêtes parallèles de métadonnées | k6 | p95 inférieur à 500 ms |

Le MVP ne dispose pas encore d’une campagne de charge complète et formalisée. Les tests ci-dessous fournissent toutefois une première indication sur le comportement de l’API dans l’environnement de développement utilisé.

## 9. Résultats des tests k6

### 9.1 Test `login-list-files.js`

**Commande exécutée :**

```powershell
k6 run login-list-files.js
```

**Configuration :** 10 VUs pendant 30 secondes, avec un arrêt progressif de 30 secondes.

| Indicateur | Résultat |
|---|---:|
| Itérations terminées | 292 |
| Requêtes HTTP | 584 |
| Débit des requêtes | 18,90 requêtes/s |
| Durée HTTP moyenne | 16,19 ms |
| Médiane HTTP | 5,61 ms |
| p90 | 21,67 ms |
| p95 | 31,92 ms |
| Erreurs HTTP | 100 % des requêtes |
| Connexion HTTP réussie | 0 / 292 |
| Liste des fichiers réussie | 0 / 292 |
| Erreur serveur 5xx | Aucune détectée |

**Interprétation :** les temps de réponse mesurés sont faibles, mais le test fonctionnel échoue entièrement : les réponses de connexion et de liste ne sont pas celles attendues. L’absence d’erreur 5xx suggère plutôt un problème de scénario, d’identifiants, d’URL, de format de requête ou de code de vérification k6 qu’une saturation du serveur. Ce test ne peut donc pas valider la performance fonctionnelle tant que les réponses attendues ne sont pas obtenues.

### 9.2 Test `metadata-download.js`

**Commande exécutée :**

```powershell
k6 run metadata-download.js
```

**Configuration :** 20 VUs pendant 20 secondes.

| Indicateur | Résultat |
|---|---:|
| Itérations terminées | 31 044 |
| Requêtes HTTP | 31 044 |
| Débit des requêtes | 1 551,19 requêtes/s |
| Durée HTTP moyenne | 12,53 ms |
| Médiane HTTP | 10,22 ms |
| p90 | 17,20 ms |
| p95 | 22,02 ms |
| Seuil k6 | Réussi : `p(95) < 500 ms` |
| Erreurs HTTP | 100 % des requêtes |
| Métadonnées retournant HTTP 200 | 0 / 31 044 |
| Erreur serveur 5xx | Aucune détectée |

**Interprétation :** le seuil de latence est respecté avec un p95 de 22,02 ms, très inférieur à l’objectif de 500 ms. Cependant, toutes les vérifications fonctionnelles échouent et aucune réponse HTTP 200 n’est obtenue. Le résultat montre donc une bonne rapidité des réponses dans cet environnement, mais ne permet pas de conclure à la disponibilité fonctionnelle de l’endpoint. Il faut vérifier le token utilisé, l’URL, les données de test et les codes HTTP attendus.

### 9.3 Test `upload-10mb.js`

**Commande exécutée :**

```powershell
k6 run upload-10mb.js
```

**Configuration :** 5 itérations partagées entre 5 VUs.

| Indicateur | Résultat |
|---|---:|
| Itérations terminées | 5 |
| Requêtes HTTP | 5 |
| Téléversements HTTP réussis | 0 / 5 |
| Erreurs HTTP | 100 % des requêtes |
| Données envoyées | 1,5 MB |
| Durée d’une itération | Environ 91 ms |

**Interprétation :** les cinq téléversements échouent. La durée très courte et l’absence de durée HTTP exploitable indiquent probablement un échec du scénario avant ou pendant la création de la requête, plutôt qu’un problème de débit d’un téléversement réel de 10 Mo. Il convient de contrôler le chemin du fichier de test, la construction du formulaire multipart, l’URL, les en-têtes et l’authentification éventuelle. Le test doit être corrigé avant de mesurer la performance réelle d’un téléversement de 10 Mo.

### 9.4 Bilan des tests k6

| Test | Performance observée | Validation fonctionnelle | Conclusion |
|---|---|---|---|
| `login-list-files.js` | p95 de 31,92 ms | Échec total | Latence faible, mais scénario ou données à corriger. |
| `metadata-download.js` | p95 de 22,02 ms ; seuil respecté | Échec total | Endpoint rapide dans l’environnement testé, mais réponses attendues non obtenues. |
| `upload-10mb.js` | Mesure non exploitable | Échec total | Requête à diagnostiquer avant toute conclusion de performance. |

**Conclusion des tests :** les résultats montrent une latence apparente faible, mais les contrôles fonctionnels échouent sur les trois scénarios. Il est donc incorrect de présenter ces tests comme une validation complète de la performance. La prochaine étape consiste à corriger les scénarios k6, confirmer les codes HTTP attendus et relancer les tests avec des données valides.

## 10. Monitoring recommandé

| Métrique | Objectif |
|---|---|
| Latence p95 des endpoints `/api/files/**` | Détecter les régressions de performance |
| Taux d’erreur HTTP 4xx / 5xx | Suivre la qualité et les incidents |
| Espace disque de `uploads/` | Éviter une saturation du disque |
| Durée du job de purge | Vérifier le bon déroulement de la purge |
| CPU pendant les téléversements massifs | Dimensionner le serveur |
| Mémoire pendant les téléchargements | Détecter les risques liés au chargement en mémoire |


## 12. Synthèse

Le MVP DataShare privilégie la simplicité avec un stockage local et un téléchargement actuellement chargé en mémoire. Les principaux leviers immédiats sont l’indexation PostgreSQL, la validation côté client, la purge nocturne et la correction des scénarios de test k6.

Les résultats k6 montrent des temps de réponse faibles dans l’environnement testé, mais les vérifications fonctionnelles échouent entièrement. Ils doivent donc être considérés comme des mesures préliminaires et non comme une validation définitive.

À moyen terme, les priorités sont le téléchargement en streaming, la pagination des listes, la supervision de la consommation mémoire et l’utilisation d’un stockage objet tel que S3 ou MinIO.
