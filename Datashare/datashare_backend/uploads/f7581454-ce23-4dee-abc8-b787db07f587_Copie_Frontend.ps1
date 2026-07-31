# ================================
# Script de copie Frontend → EtudiantFrontend
# Avec reprise automatique (robocopy)
# ================================

# Chemin source (ton projet Angular)
$source = "D:\_Formation_openclassrooms\Expert_DEVOPS\Projet2\Front-end---Testez-et-am-liorez-une-application-existante"

# Chemin destination (dépôt Git)
$destination = "D:\_Formation_openclassrooms\Expert_DEVOPS\Projet2\openclassroomsProject\EtudiantFrontend\Front-end---Testez-et-am-liorez-une-application-existante"

# Créer le dossier destination s'il n'existe pas
if (!(Test-Path -Path $destination)) {
    Write-Host "Création du dossier EtudiantFrontend..."
    New-Item -ItemType Directory -Path $destination | Out-Null
}

Write-Host "Démarrage de la copie robuste avec reprise automatique..."
Write-Host "Source      : $source"
Write-Host "Destination : $destination"

# Robocopy avec reprise automatique
robocopy $source $destination /E /Z /R:5 /W:3 /MT:8 /V /NP

# Explications des options :
# /E   → copie tout, y compris sous-dossiers vides
# /Z   → mode reprise automatique (redémarre là où il s'est arrêté)
# /R:5 → 5 tentatives max par fichier
# /W:3 → 3 secondes d’attente entre tentatives
# /MT:8 → copie multithread (8 threads, rapide)
# /V   → mode verbeux (affiche les détails)
# /NP  → pas de pourcentage (plus lisible)

Write-Host "✅ Copie terminée (ou reprise automatique effectuée)."
