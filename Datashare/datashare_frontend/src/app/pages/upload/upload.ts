import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FileService } from '../../services/file';
import { AuthService } from '../../services/auth';
import { AppHeaderBar } from '../../components/app-header/app-header';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, FormsModule, AppHeaderBar],
  templateUrl: './upload.html',
  styleUrls: ['./upload.css']
})
export class UploadComponent {

  selectedFileName = '';
  selectedFileSize = '';
  selectedFile: File | null = null;

  expirationDays = 7;
  password = '';
  tags = '';

  statusMessage = '';
  isUploading = false;

  downloadToken: string | null = null;
  downloadUrl: string | null = null;

  linkCopied = false;

  /** Aligné sur FileService backend US01 */
  private static readonly MAX_FILE_SIZE = 1024 * 1024 * 1024; // 1 Go
  private static readonly FORBIDDEN_EXTENSIONS = [
    'exe', 'bat', 'cmd', 'com', 'msi', 'scr', 'ps1', 'vbs', 'js', 'jar', 'sh', 'dll', 'sys'
  ];

  constructor(
    private fileService: FileService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * Valide taille + extension. Affiche une popup et un message si invalide.
   * @returns true si le fichier est autorisé
   */
  private validateFile(file: File): boolean {
    const name = file.name || '';
    const dot = name.lastIndexOf('.');
    const ext = dot >= 0 ? name.substring(dot + 1).toLowerCase() : '';

    if (ext && UploadComponent.FORBIDDEN_EXTENSIONS.includes(ext)) {
      const msg =
        `Type de fichier interdit : .${ext}\n\n` +
        `Extensions non autorisées :\n` +
        UploadComponent.FORBIDDEN_EXTENSIONS.map(e => '.' + e).join(', ');
      alert(msg);
      this.statusMessage = `❌ Type de fichier interdit : .${ext}`;
      return false;
    }

    if (file.size > UploadComponent.MAX_FILE_SIZE) {
      const sizeMo = (file.size / (1024 * 1024)).toFixed(1);
      const msg =
        `Fichier trop volumineux (${sizeMo} Mo).\n\n` +
        `La taille maximale autorisée est de 1 Go (1024 Mo).`;
      alert(msg);
      this.statusMessage = `❌ Fichier trop volumineux (${sizeMo} Mo). Maximum : 1 Go.`;
      return false;
    }

    return true;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validation immédiate à la sélection
      if (!this.validateFile(file)) {
        this.selectedFile = null;
        this.selectedFileName = '';
        this.selectedFileSize = '';
        input.value = ''; // permet de re-sélectionner le même fichier après
        this.cdr.detectChanges();
        return;
      }

      this.selectedFile = file;
      this.selectedFileName = file.name;
      this.selectedFileSize = `${(file.size / (1024 * 1024)).toFixed(1)} MB`;
      this.statusMessage = '';
      this.downloadUrl = null;
      this.downloadToken = null;
    }
  }

  onUpload(): void {
    console.log('Upload déclenché');

    // Vérifications
    if (!this.selectedFile) {
      this.statusMessage = 'Veuillez choisir un fichier.';
      alert('Veuillez choisir un fichier.');
      return;
    }

    // Re-validation avant envoi (sécurité)
    if (!this.validateFile(this.selectedFile)) {
      return;
    }

    if (this.password && this.password.length > 0 && this.password.length < 6) {
      this.statusMessage = 'Le mot de passe doit contenir au moins 6 caractères.';
      alert('Le mot de passe doit contenir au moins 6 caractères.');
      return;
    }

    // Validation alphanumérique (lettres et chiffres uniquement, pas de caractères spéciaux)
    if (this.password && !/^[a-zA-Z0-9]+$/.test(this.password)) {
      this.statusMessage = 'Le mot de passe doit être alphanumérique.';
      alert('Le mot de passe doit être alphanumérique.');
      return;
    }

    if (this.expirationDays < 1 || this.expirationDays > 7) {
      this.statusMessage = "L'expiration doit être comprise entre 1 et 7 jours.";
      alert("L'expiration doit être comprise entre 1 et 7 jours.");
      return;
    }

    // US07 : les anonymes peuvent uploader (sans tags)
    // US08 : tags uniquement si connecté

    // 🔥 LOG COMPLET AVANT ENVOI
    console.log('--- Données envoyées au backend ---');
    console.log('file:', this.selectedFile);
    console.log('expirationDays:', this.expirationDays);
    console.log('password:', this.password);
    console.log('tags:', this.tags);
    console.log('-----------------------------------');

    this.isUploading = true;
    this.statusMessage = 'Téléversement en cours...';
    // Efface l'ancien lien pour éviter d'afficher un token obsolète
    this.downloadToken = null;
    this.downloadUrl = null;

    const isAuth = this.authService.isAuthenticated();

    // US07 : anonyme → endpoint public ; US01 : connecté → endpoint JWT
    // US08 : tags uniquement si connecté
    const upload$ = isAuth
      ? this.fileService.uploadFile(
          this.selectedFile,
          this.expirationDays,
          this.password || undefined,
          this.tags || undefined
        )
      : this.fileService.uploadAnonymous(
          this.selectedFile,
          this.expirationDays,
          this.password || undefined
        );

    upload$.subscribe({
      next: (response: any) => {
        console.log('=== RÉPONSE UPLOAD BACKEND ===', response);
        console.log('downloadToken =', response?.downloadToken);
        console.log('downloadUrl  =', response?.downloadUrl);

        this.isUploading = false;

        // Accepte plusieurs formes de réponse (camelCase / snake_case / nested)
        const token =
          response?.downloadToken ??
          response?.download_token ??
          response?.token ??
          null;

        if (!token) {
          console.error('Aucun token trouvé dans la réponse:', response);
          this.statusMessage = '✅ Fichier téléversé, mais aucun token reçu. Ouvrez « Mes fichiers » pour récupérer le lien.';
          this.downloadToken = null;
          this.downloadUrl = null;
        } else {
          this.downloadToken = String(token);
          this.downloadUrl = `${window.location.origin}/download/${this.downloadToken}`;
          this.statusMessage = '✅ Fichier téléversé avec succès ! Lien prêt à être partagé ci-dessous.';
          console.log('Lien construit :', this.downloadUrl);
        }

        // Réinitialisation du formulaire (downloadUrl / downloadToken restent)
        this.selectedFile = null;
        this.selectedFileName = '';
        this.selectedFileSize = '';
        this.password = '';
        this.tags = '';
        this.expirationDays = 7;

        // Force la détection de changement (utile si zone.js / async edge-case)
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isUploading = false;
        console.error('Erreur lors du téléversement :', err);

        let msg = '';
        if (err?.name === 'TimeoutError') {
          msg = 'Le téléversement a expiré (timeout 30s). Essayez un fichier plus petit.';
        } else if (err?.status === 0) {
          msg = 'Serveur injoignable ou erreur CORS. Vérifiez que le backend tourne sur http://localhost:8080.';
        } else if (err?.status === 401) {
          msg = 'Session expirée. Reconnectez-vous.';
        } else if (err?.status === 403) {
          msg = 'Accès refusé.';
        } else {
          msg = err?.error?.error || err?.message || 'Erreur lors du téléversement.';
        }

        // Popup pour les erreurs métier (taille, extension, etc.)
        const lower = String(msg).toLowerCase();
        if (
          lower.includes('interdit') ||
          lower.includes('taille') ||
          lower.includes('maximum') ||
          lower.includes('mot de passe') ||
          lower.includes('expiration')
        ) {
          alert(msg);
        }

        this.statusMessage = '❌ ' + msg;
        this.cdr.detectChanges();
      },
      complete: () => {
        this.isUploading = false;
      }
    });
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  async copyLink(): Promise<void> {
    if (!this.downloadUrl) return;

    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(this.downloadUrl);
      } else {
        const textarea = document.createElement('textarea');
        textarea.value = this.downloadUrl;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }

      this.linkCopied = true;
      setTimeout(() => this.linkCopied = false, 2000);
    } catch (err) {
      console.error('Impossible de copier le lien :', err);
    }
  }
}
