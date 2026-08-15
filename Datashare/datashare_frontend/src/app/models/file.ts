/**
 * Interface représentant les métadonnées d'un fichier.
 * Alignée exactement sur l'entité 'FileEntity' du backend Spring Boot.
 */
export interface FileModel {
  /** Identifiant unique du fichier (UUID côté backend) */
  id: string;

  /** Nom du fichier stocké sur le serveur (UUID_nomOriginal) */
  filename: string;

  /** Nom d'origine du fichier téléversé par l'utilisateur */
  originalName: string;

  /** Taille du fichier en octets (bytes) */
  size: number;

  /** Type MIME du fichier (ex: application/pdf, image/png) */
  contentType: string;

  /** Date de création au format ISO string */
  createdAt: string;

  /** Date d'expiration du lien (US10) */
  expiresAt: string;

  /** UUID du propriétaire du fichier */
  ownerId: string;

  /** Token unique pour le téléchargement via lien (optionnel) */
  downloadToken?: string;

  /** US08 — Tags séparés par des virgules */
  tags?: string;
}
