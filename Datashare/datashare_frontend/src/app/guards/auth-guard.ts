// Guard d'authentification : protège les routes nécessitant un utilisateur connecté

import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  UrlTree
} from '@angular/router';
import { AuthService } from '../services/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  // Méthode appelée avant l'activation d'une route
  canActivate(): boolean | UrlTree {
    // Si l'utilisateur est authentifié, on laisse passer
    if (this.authService.isAuthenticated()) {
      return true;
    }

    // Sinon, on le redirige vers la page de login
    return this.router.parseUrl('/login');
  }
}
