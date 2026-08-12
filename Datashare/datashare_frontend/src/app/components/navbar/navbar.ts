// Barre de navigation principale (logo + liens)
// Composant standalone : peut être importé directement sans NgModule

import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
  // Titre affiché dans la navbar
  title = 'DataShare';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * 🔐 Déconnexion :
   * - supprime le token JWT et l'identifiant utilisateur
   * - redirige vers /login
   */
  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}