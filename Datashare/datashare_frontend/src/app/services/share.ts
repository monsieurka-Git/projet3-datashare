// Service de partage de fichiers : envoie les infos de partage au backend

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ShareRequest } from '../models/share-request';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  // URL correcte de l'API de partage (préfixe /api)
  private baseUrl = 'http://localhost:8080/api/share';

  constructor(private http: HttpClient) {}

  // Envoie une requête de partage au backend
  shareFile(request: ShareRequest): Observable<any> {
    return this.http.post(this.baseUrl, request);
  }
}
