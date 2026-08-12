// Page de partage de fichiers
// Composant standalone : peut être importé directement sans NgModule

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Nécessaire pour [(ngModel)]
import { FileService } from '../../services/file';
import { ShareService } from '../../services/share';

@Component({
  selector: 'app-share',
  standalone: true,
  // CommonModule : directives *ngIf / *ngFor
  // FormsModule : liaison bidirectionnelle [(ngModel)]
  imports: [CommonModule, FormsModule],
  templateUrl: './share.html',
  styleUrls: ['./share.css'],
})
export class Share {
  // Fichier sélectionné par l'utilisateur
  selectedFile: File | null = null;

  // Nom du fichier sélectionné (affiché dans le template)
  selectedFileName = '';

  // Email du destinataire (lié via ngModel)
  recipientEmail = '';

  // Message de statut affiché à l'utilisateur
  statusMessage = '';

  // Lien de téléchargement renvoyé après un partage réussi
  shareDownloadUrl: string | null = null;

  constructor(
    private fileService: FileService,
    private shareService: ShareService
  ) {}

  // Récupère le fichier choisi dans l'input de type file
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.selectedFile = file;
    this.selectedFileName = file ? file.name : '';
    this.statusMessage = '';
    this.shareDownloadUrl = null;
  }

  // Téléverse le fichier puis le partage avec le destinataire
  onShare(): void {
    if (!this.selectedFile) {
      this.statusMessage = 'Veuillez sélectionner un fichier.';
      return;
    }

    if (!this.recipientEmail) {
      this.statusMessage = "Veuillez saisir l'email du destinataire.";
      return;
    }

    this.statusMessage = 'Partage en cours...';
    this.shareDownloadUrl = null;

    // 1) Upload du fichier -> le backend renvoie l'id du fichier créé
    this.fileService.uploadFile(this.selectedFile).subscribe({
      next: (uploaded: any) => {
        const fileId = uploaded?.id;
        if (!fileId) {
          this.statusMessage = 'Upload OK mais identifiant fichier manquant.';
          return;
        }

        // 2) Partage du fichier avec le destinataire
        this.shareService
          .shareFile({
            fileId: String(fileId),
            recipientEmail: this.recipientEmail,
          })
          .subscribe({
            next: (res: any) => {
              this.statusMessage = res?.message || `Fichier partagé avec ${this.recipientEmail}.`;
              // Construit l'URL absolue vers la page Angular /download/:token
              const token = res?.downloadToken;
              if (token) {
                this.shareDownloadUrl = `${window.location.origin}/download/${token}`;
              } else if (res?.downloadUrl) {
                this.shareDownloadUrl = res.downloadUrl.startsWith('http')
                  ? res.downloadUrl
                  : `${window.location.origin}${res.downloadUrl}`;
              }
            },
            error: (err) => {
              console.error('Erreur partage :', err);
              this.statusMessage = err?.error?.error || 'Erreur lors du partage du fichier.';
            },
          });
      },
      error: (err) => {
        console.error('Erreur upload (share) :', err);
        this.statusMessage = err?.error?.error || 'Erreur lors du téléversement du fichier.';
      },
    });
  }
}
