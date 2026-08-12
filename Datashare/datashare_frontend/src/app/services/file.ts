import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, timeout, catchError, throwError } from 'rxjs';
import { FileModel } from '../models/file';

// Interface représentant les métadonnées renvoyées par l'endpoint /api/files/metadata/{token}
export interface FileMetadata {
  originalName: string;
  size: number;
  contentType: string;
  expiresAt: string;
  passwordProtected: boolean;
}
export interface UploadDto {
  id: string;
  filename: string;
  originalName: string;
  downloadToken: string;
  downloadUrl: string | null;
}

@Injectable({
  providedIn: 'root' // Service disponible dans toute l'application (singleton)
})
export class FileService {
  /** URL de base de l'API REST Spring Boot */
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Récupère la liste des fichiers de l'utilisateur connecté (via JWT).
   * @returns Observable émettant le tableau des fichiers (FileModel[])
   */
  getFiles(): Observable<FileModel[]> {
    // Appelle l'endpoint GET /api/files du backend Spring Boot
    // Le backend identifie l'utilisateur via le token JWT (header Authorization)
    return this.http.get<FileModel[]>(`${this.apiUrl}/files`);
  }

  /**
   * Récupère la liste des métadonnées des fichiers appartenant à un utilisateur.
   * @param userId Identifiant de l'utilisateur
   * @returns Observable émettant le tableau des fichiers (FileModel[])
   */
  getUserFiles(userId: number | string): Observable<FileModel[]> {
    // Appelle l'endpoint GET /api/files du backend Spring Boot
    // Le paramètre userId est ignoré côté backend qui utilise le JWT
    return this.http.get<FileModel[]>(`${this.apiUrl}/files`);
  }

  /**
   * US01 — Téléverse un fichier vers le backend avec les options avancées.
   * @param file Fichier à téléverser
   * @param expiresInDays Nombre de jours avant expiration (1-7, défaut 7)
   * @param password Mot de passe optionnel pour protéger le téléchargement (≥ 6 caractères)
   * @param tags Tags optionnels séparés par des virgules
   * @returns Observable de la réponse HTTP
   */
  uploadFile(
    file: File,
    expiresInDays?: number,
    password?: string,
    tags?: string
  ): Observable<UploadDto> {
    const formData = new FormData();
    formData.append('file', file);

    if (expiresInDays !== undefined) formData.append('expiresInDays', String(expiresInDays));
    if (password?.trim()) formData.append('password', password);
    if (tags?.trim()) formData.append('tags', tags);

    // 🔥 Timeout de 30s + gestion d'erreur pour éviter le blocage infini
    return this.http.post<UploadDto>(`${this.apiUrl}/files/upload`, formData).pipe(
      timeout(30000),
      catchError((err) => {
        console.error('Erreur upload (timeout ou réseau) :', err);
        return throwError(() => err);
      })
    );
  }


  /**
   * US07 — Upload anonyme (sans JWT).
   * Mêmes options que l'upload connecté sauf les tags (US08 réservé aux connectés).
   */
  uploadAnonymous(
    file: File,
    expiresInDays?: number,
    password?: string
  ): Observable<UploadDto> {
    const formData = new FormData();
    formData.append('file', file);
    if (expiresInDays !== undefined) formData.append('expiresInDays', String(expiresInDays));
    if (password?.trim()) formData.append('password', password);

    return this.http.post<UploadDto>(`${this.apiUrl}/files/upload/anonymous`, formData).pipe(
      timeout(30000),
      catchError((err) => {
        console.error('Erreur upload anonyme :', err);
        return throwError(() => err);
      })
    );
  }

  /**
   * US08 — Met à jour les tags d'un fichier.
   */
  updateTags(fileId: string, tags: string): Observable<any> {
    const params = new HttpParams().set('tags', tags);
    return this.http.put(`${this.apiUrl}/files/${fileId}/tags`, null, { params });
  }

  /**
   * Envoie une requête de suppression pour un fichier donné.
   * @param fileId Identifiant UUID du fichier
   * @returns Observable de la réponse HTTP
   */
  deleteFile(fileId: string): Observable<void> {
    // Appelle l'endpoint DELETE /api/files/info/{fileId} du backend Spring Boot
    return this.http.delete<void>(`${this.apiUrl}/files/info/${fileId}`);
  }

  /**
   * US02 — Récupère les métadonnées d'un fichier via son token unique.
   * Accessible SANS authentification.
   * @param token Token unique de téléchargement
   * @returns Observable des métadonnées du fichier
   */
  getFileMetadata(token: string): Observable<FileMetadata> {
    // Appelle l'endpoint GET /api/files/metadata/{token} du backend Spring Boot
    return this.http.get<FileMetadata>(`${this.apiUrl}/files/metadata/${token}`);
  }

  /**
   * US02 — Télécharge un fichier via son token unique.
   * Accessible SANS authentification.
   * @param token Token unique de téléchargement
   * @param password Mot de passe (requis si le fichier est protégé)
   * @returns Observable du contenu binaire du fichier
   */
  downloadFile(token: string, password?: string): Observable<Blob> {
    // Construit les paramètres : le mot de passe est optionnel
    let params = new HttpParams();
    if (password) {
      params = params.set('password', password);
    }

    // Appelle l'endpoint POST /api/files/download/{token} du backend Spring Boot
    // Réponse en Blob pour permettre le téléchargement côté navigateur
    return this.http.post(`${this.apiUrl}/files/download/${token}`, null, {
      params,
      responseType: 'blob'
    });
  }
}