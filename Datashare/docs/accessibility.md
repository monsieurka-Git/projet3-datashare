# Accessibilité — Synthèse RGAA / WCAG

DataShare applique les bonnes pratiques RGAA 4.1 / WCAG 2.1 AA sur le parcours critique :
**Connexion → Téléversement → Génération du lien → Téléchargement**.

## 1. Structure & Navigation
- `aria-label` sur les icônes (upload, logout, navigation).
- `alt` descriptif sur le logo.
- Onglets accessibles : `role="tab"`, `aria-selected`, `aria-current`.
- Navigation clavier complète.

## 2. Formulaires
- Labels explicites ou `aria-label`.
- Champs obligatoires : `aria-required="true"`.
- Erreurs vocalisées : `role="alert"`.
- Messages dynamiques : `aria-live="assertive"`.

## 3. Composants interactifs
- Boutons avec noms accessibles : copier, supprimer, accéder, téléverser.
- Icônes décoratives masquées : `aria-hidden="true"`.

## 4. Feedback utilisateur
- Chargement : `aria-live="polite"`.
- Succès : `role="status"`.
- Erreur : `role="alert"`.

## 5. Tableau RGAA → Actions DataShare

| Critère RGAA | Exigence | Implémentation | Exemple |
|--------------|----------|----------------|---------|
| 1.1 | Texte alternatif | `alt` descriptif | Logo |
| 3.1 | Labels | `label for="email"` | Formulaire |
| 3.2 | Erreurs textuelles | `role="alert"` | Erreur login |
| 4.1 | Nom accessible | `aria-label` | Boutons |
| 7.1 | Focus visible | Styles CSS | Inputs / boutons |
| 8.2 | Messages dynamiques | `aria-live` | Upload / download |
| 9.3 | Navigation clavier | `role="tab"` | Onglets |
| 10.7 | Icônes décoratives | `aria-hidden="true"` | Icônes SVG |


## 6. Test NVDA — Synthèse
- Email → « Adresse email, zone de saisie, obligatoire »
- Mot de passe → « Mot de passe, zone de saisie, obligatoire »
- Téléversement → « Téléversement en cours… » puis « Lien de téléchargement généré »
- Téléchargement → « Téléchargement… » ou « Lien invalide ou expiré »

## 7. Phrase de soutenance
« DataShare respecte les bonnes pratiques RGAA/WCAG : labels explicites, noms accessibles, messages vocalisés, navigation clavier et feedback dynamique. Parcours validé avec NVDA. »

