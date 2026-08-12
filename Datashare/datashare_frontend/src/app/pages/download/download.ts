// Page "Télécharger un fichier" : affiche les métadonnées et permet le téléchargement (US02)

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FileService, FileMetadata } from '../../services/file';
import { AppHeaderBar } from '../../components/app-header/app-header';
import { timeout, catchError, of } from 'rxjs';

@Component({
  selector: 'app-download',
  standalone: true,
  imports: [CommonModule, FormsModule, AppHeaderBar],
  templateUrl: './download.html',
  styleUrls: ['./download.css']
})
export class DownloadComponent implements OnInit {

  // Token unique de téléchargement (récupéré depuis l'URL)
  token = '';

  // Métadonnées du fichier chargées depuis le backend
  fileMetadata: FileMetadata | null = null;

  // Mot de passe saisi par l'utilisateur (si le fichier est protégé)
  password = '';

  // Message de statut / erreur
  statusMessage = '';
  isLoading = false;
  isDownloading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fileService: FileService,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * Récupère le token depuis l'URL et charge les métadonnées.
   */
  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.token = params['token'] || '';
      console.log('[Download] token depuis URL =', this.token);
      if (this.token) {
        this.loadMetadata();
      } else {
        this.isLoading = false;
        this.statusMessage = 'Aucun lien de téléchargement valide.';
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * US02 — Charge les métadonnées du fichier depuis le backend.
   * Permet d'afficher le nom, la taille, le type et l'expiration.
   */
  private loadMetadata(): void {
    this.isLoading = true;
    this.statusMessage = '';
    this.fileMetadata = null;
    console.log('[Download] Appel GET /api/files/metadata/' + this.token);

    this.fileService.getFileMetadata(this.token).pipe(
      timeout(15000),
      catchError((err) => {
        console.error('[Download] Erreur metadata :', err);
        this.isLoading = false;
        this.fileMetadata = null;

        // Extraire un message lisible (le backend renvoie { error, timestamp })
        let msg = 'Lien invalide ou expiré.';
        if (err?.name === 'TimeoutError') {
          msg = 'Le serveur ne répond pas (timeout). Vérifiez que le backend tourne.';
        } else if (err?.status === 0) {
          msg = 'Impossible de joindre le serveur (CORS ou backend arrêté).';
        } else if (err?.status === 404 || err?.status === 410) {
          msg = err?.error?.error || 'Lien invalide ou expiré.';
        } else if (err?.error?.error) {
          msg = err.error.error;
        } else if (typeof err?.error === 'string') {
          msg = err.error;
        }
        this.statusMessage = msg;
        this.cdr.detectChanges();
        return of(null);
      })
    ).subscribe({
      next: (metadata: FileMetadata | null) => {
        if (metadata) {
          console.log('[Download] Métadonnées reçues :', metadata);
          this.fileMetadata = metadata;
          this.statusMessage = '';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * US02 — Télécharge le fichier.
   * Si le fichier est protégé par mot de passe, le mot de passe est envoyé.
   */
  onDownload(): void {
    if (!this.token || !this.fileMetadata) {
      return;
    }

    // Vérifie que le mot de passe est saisi si le fichier est protégé
    if (this.fileMetadata.passwordProtected && !this.password) {
      this.statusMessage = 'Ce fichier est protégé. Veuillez saisir le mot de passe.';
      return;
    }

    this.isDownloading = true;
    this.statusMessage = 'Téléchargement en cours...';
    this.cdr.detectChanges();

    this.fileService.downloadFile(this.token, this.password || undefined).subscribe({
      next: (blob: Blob) => {
        // Si le backend a renvoyé une erreur JSON sous forme de Blob (responseType: blob)
        if (blob && blob.type && blob.type.includes('application/json')) {
          this.isDownloading = false;
          this.statusMessage = 'Erreur lors du téléchargement du fichier.';
          this.cdr.detectChanges();
          blob.text().then((txt) => {
            try {
              const j = JSON.parse(txt);
              if (j?.error) this.statusMessage = j.error;
            } catch { /* ignore */ }
            this.cdr.detectChanges();
          });
          return;
        }

        // 📥 Déclenche le téléchargement dans le navigateur
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = this.fileMetadata?.originalName || 'fichier';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.isDownloading = false;
        this.statusMessage = '✅ Téléchargement réussi !';
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.isDownloading = false;
        console.error('Erreur lors du téléchargement :', err);

        // Avec responseType blob, le corps d'erreur est parfois un Blob
        const finish = (errorText: string) => {
          if (errorText.includes('Mot de passe') || errorText.includes('incorrect')) {
            this.statusMessage = 'Mot de passe incorrect. Veuillez réessayer.';
          } else if (errorText.includes('expiré')) {
            this.statusMessage = 'Ce lien a expiré.';
          } else if (errorText.includes('invalide') || errorText.includes('introuvable')) {
            this.statusMessage = 'Ce lien est invalide ou introuvable.';
          } else {
            this.statusMessage = 'Erreur lors du téléchargement du fichier.';
          }
          this.cdr.detectChanges();
        };

        if (err?.error instanceof Blob) {
          err.error.text().then((txt: string) => {
            try {
              const j = JSON.parse(txt);
              finish(j?.error || txt || '');
            } catch {
              finish(txt || err?.message || '');
            }
          });
        } else {
          const errorText = err?.error?.error || err?.error?.text || err?.error || err?.message || '';
          finish(String(errorText));
        }
      },
      complete: () => {
        // Sécurité : ne jamais laisser le bouton bloqué sur "Téléchargement..."
        if (this.isDownloading) {
          this.isDownloading = false;
          this.cdr.detectChanges();
        }
      }
    });
  }
}
