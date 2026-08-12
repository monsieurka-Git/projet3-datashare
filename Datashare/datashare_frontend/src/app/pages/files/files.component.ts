import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileService } from '../../services/file';
import { AuthService } from '../../services/auth';
import { FileModel } from '../../models/file';

@Component({
  selector: 'app-my-files',
  standalone: true, // Composant autonome (Angular 14+)
  imports: [CommonModule], // Permet l'utilisation des directives *ngIf, *ngFor et du pipe 'date'
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.css']
})
export class MyFilesComponent implements OnInit {
  /** Liste des fichiers appartenant à l'utilisateur connecté */
  files: FileModel[] = [];

  /** Indicateur d'état de chargement pour afficher un spinner à l'écran */
  loading = true;

  /** Message d'erreur à afficher en cas d'échec de la requête HTTP */
  errorMessage = '';
  
  /** 
   * Injection de l'objet natif JavaScript 'Math' dans le composant.
   * Nécessaire pour pouvoir utiliser Math dans le template HTML (ex: calculs).
   */
  Math = Math;

  /** Id du fichier dont le lien vient d'être copié (pour afficher un message temporaire) */
  copiedFileId: string | null = null;

  constructor(
    private fileService: FileService, // Service d'interaction avec les API fichiers
    private authService: AuthService  // Service gérant la session et l'utilisateur connecté
  ) {}

  /**
   * Hook du cycle de vie d'Angular déclenché automatiquement à l'initialisation du composant.
   */
  ngOnInit(): void {
    this.loadUserFiles();
  }

  /**
   * Récupère l'identifiant de l'utilisateur actuel et charge sa liste de fichiers.
   */
  loadUserFiles(): void {
    const userId = this.authService.getCurrentUserId();

    // Vérification de la présence de la session utilisateur
    if (!userId) {
      this.errorMessage = 'Utilisateur non connecté.';
      this.loading = false;
      return;
    }

    // Appel au service backend pour récupérer les métadonnées des fichiers
    this.fileService.getUserFiles(userId).subscribe({
      next: (data) => {
        this.files = data;      // Stockage de la liste reçue
        this.loading = false;    // Fin du chargement
      },
      error: (err) => {
        console.error('Erreur lors du chargement des fichiers :', err);
        this.errorMessage = 'Impossible de charger vos fichiers.';
        this.loading = false;    // Fin du chargement même en cas d'erreur
      }
    });
  }

  /**
   * 🐛 FIX — Construit le lien de partage d'un fichier déjà téléversé.
   * Jusqu'ici, "Mes fichiers" ne proposait AUCUN moyen de récupérer ce lien
   * après l'upload (seul le bouton "Supprimer" existait) : le fichier était
   * bien stocké, mais impossible de le repartager sans re-téléverser.
   */
  getDownloadLink(file: FileModel): string {
    if (!file.downloadToken) {
      return '';
    }
    return `${window.location.origin}/download/${file.downloadToken}`;
  }

  copyLink(file: FileModel): void {
    const link = this.getDownloadLink(file);
    if (!link) {
      return;
    }
    navigator.clipboard.writeText(link).then(() => {
      this.copiedFileId = file.id;
      setTimeout(() => {
        if (this.copiedFileId === file.id) {
          this.copiedFileId = null;
        }
      }, 2000);
    });
  }

  /**
   * Supprime un fichier à partir de son ID après confirmation de l'utilisateur.
   * @param fileId Identifiant unique du fichier à supprimer
   */
  deleteFile(fileId: string): void {
    if (confirm('Voulez-vous vraiment supprimer ce fichier ?')) {
      this.fileService.deleteFile(fileId).subscribe({
        next: () => {
          // Mise à jour locale du tableau pour retirer le fichier sans recharger toute la page
          this.files = this.files.filter(f => f.id !== fileId);
        },
        error: (err) => {
          console.error('Erreur lors de la suppression :', err);
        }
      });
    }
  }
}