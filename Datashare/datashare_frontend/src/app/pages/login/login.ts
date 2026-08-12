// Page de connexion : formulaire email + mot de passe
// Composant standalone : peut être importé directement sans NgModule

import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AppHeaderBar } from '../../components/app-header/app-header';
import { FormsModule } from '@angular/forms';
import { AuthService, AuthResponse } from '../../services/auth';
import { LoginRequest } from '../../models/login-request';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, AppHeaderBar],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {

  /**
   * Modèle du formulaire de login.
   * Ce modèle DOIT être envoyé au backend.
   */
  form: LoginRequest = {
    email: '',
    password: ''
  };

  // Message d'erreur affiché en cas d'échec de connexion
  errorMessage = '';

  /** Empêche le double-submit */
  isSubmitting = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * US03 — Redirige vers la page d'inscription.
   */
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  /**
   * Soumission du formulaire de connexion.
   * - Envoie les identifiants au backend
   * - Sauvegarde le token JWT
   * - Redirige vers /home
   */
  onSubmit(): void {
    if (this.isSubmitting) {
      return;
    }

    this.errorMessage = '';
    this.isSubmitting = true;
    this.cdr.detectChanges();

    this.authService.login(this.form).subscribe({
      next: (response: AuthResponse) => {
        this.authService.saveToken(response.token);
        this.authService.saveUserId(response.userId);
        this.isSubmitting = false;
        this.router.navigate(['/home']);
      },
      error: () => {
        this.errorMessage = 'Identifiants incorrects. Veuillez réessayer.';
        this.isSubmitting = false;
        // Affiche le message immédiatement (sans attendre un autre événement UI)
        this.cdr.detectChanges();
      }
    });
  }
}
