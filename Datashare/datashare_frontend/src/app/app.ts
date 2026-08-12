// Composant racine de l'application DataShare
// Composant standalone : bootstrap direct sans NgModule

import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';

// Composants globaux
import { NavbarComponent } from './components/navbar/navbar';
import { ModalComponent } from './components/modal/modal';
import { AuthService } from './services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, ModalComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  // Affiche la navbar uniquement sur les pages authentifiées.
  showNavbar = false;

  // État de la modale : masquée par défaut
  isModalVisible = false;

  // Contenu de la modale
  modalTitle = '';
  modalMessage = '';

  constructor(private authService: AuthService, private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event) => {
        const navigationEnd = event as NavigationEnd;
        this.showNavbar = this.shouldShowNavbar(navigationEnd.urlAfterRedirects);
      });

    this.showNavbar = this.shouldShowNavbar(this.router.url);
  }

  /**
   * Retourne vrai si la navbar doit s'afficher sur la route actuelle.
   */
  private shouldShowNavbar(url: string): boolean {
    // Header géré page par page via <app-header-bar> (Se déconnecter partout)
    return false;
  }

  // Ouvre la modale avec un titre et un message
  openModal(title: string, message: string): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.isModalVisible = true;
  }

  // Ferme la modale
  onModalClose(): void {
    this.isModalVisible = false;
  }

  // Action de confirmation
  onModalConfirm(): void {
    this.isModalVisible = false;
  }
}