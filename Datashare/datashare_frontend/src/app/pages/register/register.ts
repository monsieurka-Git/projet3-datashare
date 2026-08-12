// Page d'inscription : formulaire email + mot de passe (US03)
// Composant standalone : peut être importé directement sans NgModule

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AppHeaderBar } from '../../components/app-header/app-header';
// RouterLink added for template links
import { FormsModule } from '@angular/forms';
import { AuthService, RegisterRequest } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, AppHeaderBar],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {

  /**
   * Modèle du formulaire d'inscription.
   * Ce modèle DOIT être envoyé au backend.
   */
  form: RegisterRequest = {
    email: '',
    password: ''
  };

  // Confirmation du mot de passe (vérifié côté client uniquement)
  confirmPassword = '';

  // Message d'erreur affiché en cas d'échec d'inscription
  errorMessage = '';

  // Message de succès affiché après une inscription réussie
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Soumission du formulaire d'inscription.
   * - Vérifie que les mots de passe correspondent
   * - Vérifie que le mot de passe fait au moins 8 caractères (US03)
   * - Envoie les données au backend
   * - Redirige vers /login
   */
  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    // Vérification : email requis
    if (!this.form.email || !this.form.email.trim()) {
      this.errorMessage = "L'email est requis.";
      return;
    }

    // Vérification : format email valide (US03)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.form.email)) {
      this.errorMessage = "Veuillez saisir un email valide.";
      return;
    }

    // Vérification : mot de passe minimum 8 caractères (US03)
    if (!this.form.password || this.form.password.length < 8) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 8 caractères.';
      return;
    }

    // Vérification : confirmation du mot de passe
    if (this.form.password !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas.';
      return;
    }

    // Envoi de la requête d'inscription au backend
    this.authService.register(this.form).subscribe({
      next: () => {
        this.successMessage = 'Compte créé avec succès ! Redirection vers la connexion...';
        // Redirection vers la page de connexion après 2 secondes
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        console.error('Erreur lors de l\'inscription :', err);
        // Message d'erreur lisible (l'email est probablement déjà utilisé)
        this.errorMessage = err?.error || "L'inscription a échoué. Veuillez réessayer.";
      }
    });
  }

  /**
   * Retourne à la page de connexion.
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}