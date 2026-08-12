// Interceptor HTTP : ajoute le token JWT à chaque requête sortante

import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth';

// Fonction interceptor : ajoute le token JWT à chaque requête sortante
// SAUF les endpoints publics de téléchargement / métadonnées
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);

  // Endpoints publics US02 — ne pas envoyer le JWT
  const url = req.url;
  if (
    url.includes('/api/files/metadata/') ||
    url.includes('/api/files/download/') ||
    url.includes('/api/files/upload/anonymous')
  ) {
    return next(req);
  }

  const token = authService.getToken();

  // Si un token existe, on clone la requête en ajoutant le header Authorization
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  // Sinon, on laisse passer la requête telle quelle
  return next(req);
};