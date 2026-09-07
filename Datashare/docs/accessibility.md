# Accessibilité

## 1. Objectif

1. Rendre l’interface utilisable avec un lecteur d’écran et au clavier.
2. Niveau visé pour le MVP : bonnes pratiques de base (pas encore un audit WCAG complet).

## 2. Mesures en place

1. Labels sur les champs de formulaires (login, register, upload).
2. Attributs `aria-label` sur les actions principales de l’en-tête (connexion, déconnexion).
3. Messages d’erreur en texte, pas seulement par la couleur.
4. Structure de pages avec titres hiérarchiques.

## 3. Limites et suites

1. Pas encore de contrôle automatisé (axe) en CI.
2. Amélioration possible : focus visible renforcé, tests clavier systématiques.
