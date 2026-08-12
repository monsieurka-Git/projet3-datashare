// Modèle pour la requête de partage de fichier

export interface ShareRequest {
  // Identifiant du fichier à partager (UUID string côté backend)
  fileId: string;

  // Email du destinataire
  recipientEmail: string;
}
