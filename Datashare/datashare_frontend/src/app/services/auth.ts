// Service d'authentification : gère la connexion, l'inscription et le token JWT

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../models/login-request';

// Interface correspondant EXACTEMENT à la réponse du backend
export interface AuthResponse {
  token: string;
  type: string;
  expiresAt: number;
  userId: string;
  email: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // URL de base de l'API d'authentification
  private baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Envoie la requête de login au backend.
   * Le backend renvoie un AuthResponse complet.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request);
  }

  /**
   * US03 — Envoie la requête d'inscription au backend.
   * Le backend renvoie un message de succès.
   */
  register(request: RegisterRequest): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/register`, request, { responseType: 'text' as 'json' });
  }

  /**
   * Sauvegarde le token JWT et l'identifiant utilisateur dans le localStorage.
   */
  saveToken(token: string): void {
    if (token && token.trim() !== '') {
      localStorage.setItem('token', token);
    }
  }

  /**
   * Sauvegarde l'identifiant de l'utilisateur connecté.
   */
  saveUserId(userId: string): void {
    if (userId && userId.trim() !== '') {
      localStorage.setItem('userId', userId);
    }
  }

  /**
   * Récupère l'identifiant de l'utilisateur connecté depuis le localStorage.
   * @returns L'identifiant utilisateur (string) ou null si non connecté.
   */
  getCurrentUserId(): string | null {
    return localStorage.getItem('userId');
  }

  /**
   * Récupère le token JWT depuis le localStorage.
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Supprime le token et l'identifiant utilisateur (déconnexion).
   */
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
  }

  /**
   * Indique si l'utilisateur est connecté.
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    return token !== null && token.trim() !== '';
  }
}