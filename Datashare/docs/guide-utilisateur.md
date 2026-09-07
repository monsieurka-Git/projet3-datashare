# Guide utilisateur

## 1. Accéder à l’application

1. Ouvrir l’adresse du front en local (lien actif si frontend started) : [http://localhost:4200](http://localhost:4200).
2. La page d’accueil propose la connexion ou l’envoi d’un fichier.

## 2. Créer un compte

1. Ouvrir la page **Inscription**.
2. Saisir un e-mail valide et un mot de passe d’au moins **8 caractères**.
3. Confirmer le mot de passe si demandé.
4. Valider : le compte est créé en base.

## 3. Se connecter

1. Ouvrir la page **Connexion**.
2. Saisir e-mail et mot de passe.
3. En cas d’erreur, un message indique que les identifiants sont incorrects.
4. Après succès, accès à **Mes fichiers** et à l’upload connecté.

## 4. Envoyer un fichier (compte connecté)

1. Aller sur la page d’**upload**.
2. Sélectionner un fichier (respecter taille max et extensions autorisées).
3. Optionnel : durée d’expiration (1 à 7 jours), mot de passe, tags.
4. Cliquer sur **Téléverser**.
5. Copier le **lien de téléchargement** affiché pour le partager.

## 5. Envoyer un fichier sans compte

1. Accéder à l’upload **sans être connecté**.
2. Déposer le fichier et valider.
3. Récupérer le lien unique.
4. Limitation : pas d’historique ni de suppression ultérieure depuis l’interface.

## 6. Télécharger un fichier reçu

1. Ouvrir le lien reçu dans le navigateur.
2. Consulter le nom et la taille du fichier.
3. Saisir le mot de passe si le fichier est protégé.
4. Lancer le téléchargement.
5. Si le lien a expiré, un message d’erreur s’affiche.

## 7. Gérer « Mes fichiers »

1. Une fois connecté, ouvrir **Mes fichiers** (`/home`).
2. Consulter la liste des fichiers envoyés.
3. Pour supprimer : cliquer sur supprimer, puis **confirmer** dans la fenêtre.
4. Utiliser **Se déconnecter** dans l’en-tête pour quitter la session.

## 8. Points d’attention

1. Un fichier non récupéré avant expiration est supprimé automatiquement.
2. Le mot de passe d’un fichier ne peut pas être récupéré (hashé côté serveur).
3. Les messages d’erreur (taille, extension, login) s’affichent à l’écran.
