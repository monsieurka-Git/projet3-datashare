import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

/**
 * Header global DataShare (maquettes Figma).
 * - Logo toujours visible
 * - Si connecté : « Mes fichiers » + « Se déconnecter » (+ optionnel Ajouter)
 * - Si non connecté : « Se connecter »
 */
@Component({
  selector: 'app-header-bar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './app-header.html',
  styleUrls: ['./app-header.css']
})
export class AppHeaderBar {
  /** Affiche le lien « + Ajouter des fichiers » (pages espace connecté) */
  @Input() showAddFiles = false;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  get isLoggedIn(): boolean {
    return this.auth.isAuthenticated();
  }

  onLogout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
