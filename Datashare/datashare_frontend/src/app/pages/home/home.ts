import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FileService } from '../../services/file';
import { FileModel } from '../../models/file';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { ModalComponent } from '../../components/modal/modal';
import { AppHeaderBar } from '../../components/app-header/app-header';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, AppHeaderBar],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {

  // Liste des fichiers de l'utilisateur
  files: FileModel[] = [];

  /** Chargement en cours de la liste */
  isLoading = false;

  /** Message d'erreur API (null si OK) */
  loadError: string | null = null;

  // Onglet actif pour l'affichage des fichiers
  activeTab: 'Tous' | 'Actifs' | 'Expirés' = 'Tous';

  /** US08 — filtre texte sur les tags */
  tagFilter = '';

  // Modal de confirmation de suppression (US06)
  showDeleteModal = false;
  fileToDelete: FileModel | null = null;
  /** Empêche le double-clic sur Confirmer */
  isDeleting = false;

  /** Id du fichier dont le lien vient d'être copié */
  copiedId: string | null = null;

  constructor(
    private fileService: FileService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * ngOnInit est appelé quand le composant est initialisé.
   * C'est ici qu'on déclenche le chargement des fichiers.
   */
  ngOnInit() {
    this.loadFiles();
  }

  /**
   * Charge les fichiers de l'utilisateur connecté depuis le backend.
   * Si l'identifiant utilisateur est absent, on charge tous les fichiers.
   */
  private loadFiles(): void {
    this.isLoading = true;
    this.loadError = null;

    // Toujours GET /api/files : le backend filtre par JWT (ownerId)
    this.fileService.getFiles().subscribe({
      next: (data: FileModel[] | unknown) => {
        // Normalise la réponse (toujours un tableau)
        if (Array.isArray(data)) {
          this.files = data as FileModel[];
        } else if (data && typeof data === 'object' && Array.isArray((data as any).content)) {
          this.files = (data as any).content;
        } else {
          this.files = [];
        }
        this.isLoading = false;
        this.loadError = null;
        // Force le rafraîchissement UI (évite de rester bloqué sur « Chargement… »
        // jusqu'au prochain clic d'onglet)
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erreur de chargement des fichiers', err);
        this.files = [];
        this.isLoading = false;
        if (err?.status === 401 || err?.status === 403) {
          this.loadError = 'Session expirée ou non autorisée. Reconnectez-vous.';
        } else if (err?.status === 0) {
          this.loadError = 'Impossible de joindre le serveur (backend arrêté ou CORS).';
        } else {
          this.loadError =
            err?.error?.error ||
            err?.error?.message ||
            'Impossible de charger vos fichiers. Réessayez.';
        }
        this.cdr.detectChanges();
      }
    });
  }

  /** Recharge la liste (bouton Réessayer) */
  refreshFiles(): void {
    this.loadFiles();
  }

  /**
   * Sélectionne l'onglet actif.
   */
  selectTab(tab: 'Tous' | 'Actifs' | 'Expirés'): void {
    this.activeTab = tab;
  }

  /**
   * US05 — Retourne la liste des fichiers à afficher selon l'onglet actif.
   * - 'Tous' : tous les fichiers
   * - 'Actifs' : fichiers non expirés
   * - 'Expirés' : fichiers expirés
   */
  get displayedFiles(): FileModel[] {
    const now = new Date().toISOString();
    let list = this.files;

    if (this.activeTab === 'Actifs') {
      list = list.filter(f => !f.expiresAt || f.expiresAt >= now);
    } else if (this.activeTab === 'Expirés') {
      list = list.filter(f => f.expiresAt && f.expiresAt < now);
    }

    // US08 — filtre optionnel par tag
    const q = (this.tagFilter || '').trim().toLowerCase();
    if (q) {
      list = list.filter(f => (f.tags || '').toLowerCase().includes(q));
    }
    return list;
  }

  /** Affiche les tags d'un fichier sous forme de pastilles */
  tagList(file: FileModel): string[] {
    if (!file.tags) return [];
    return file.tags.split(',').map(t => t.trim()).filter(Boolean);
  }

  /**
   * US06 — Ouvre la modale de confirmation avant suppression.
   */
  confirmDelete(file: FileModel): void {
    this.fileToDelete = file;
    this.showDeleteModal = true;
  }

  /**
   * US06 — Annule la suppression (ferme la modale).
   */
  onDeleteCancel(): void {
    this.showDeleteModal = false;
    this.fileToDelete = null;
  }

  /**
   * US06 — Supprime le fichier confirmé et met à jour la liste localement.
   * La suppression est irréversible (conformité US06).
   * Ferme la modale immédiatement pour éviter le double-clic.
   */
  onDeleteConfirm(): void {
    if (!this.fileToDelete || this.isDeleting) {
      return;
    }

    const toDelete = this.fileToDelete;
    this.isDeleting = true;

    // Ferme la modale tout de suite (évite le 2e clic)
    this.showDeleteModal = false;
    this.fileToDelete = null;

    // Suppression optimiste : retire immédiatement de la liste affichée
    const previous = this.files;
    this.files = this.files.filter(f => f.id !== toDelete.id);
    this.cdr.detectChanges();

    this.fileService.deleteFile(toDelete.id).subscribe({
      next: () => {
        this.isDeleting = false;
        // Rafraîchit la liste depuis le backend pour garantir la cohérence
        this.loadFiles();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur suppression :', err);
        // Restaure la liste si l'API échoue
        this.files = previous;
        this.isDeleting = false;
        this.cdr.detectChanges();
        alert(err?.error?.error || 'Impossible de supprimer ce fichier.');
      }
    });
  }

  /**
   * US02 — Accès au fichier via son lien de téléchargement.
   * Redirige vers la page de téléchargement avec le token unique.
   */
  onAccess(file: FileModel): void {
    const token = file.downloadToken;
    if (!token) {
      console.error('Aucun token de téléchargement pour ce fichier', file);
      alert('Ce fichier ne dispose pas de lien de téléchargement.');
      return;
    }
    // Navigation Angular vers la page publique de téléchargement
    this.router.navigate(['/download', token]);
  }

  isExpired(file: FileModel): boolean {
    if (!file.expiresAt) return false;
    return file.expiresAt < new Date().toISOString();
  }

  /**
   * 🔐 Déconnexion :
   * - supprime le token JWT
   * - redirige proprement vers /login via le router Angular
   */
  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  copyLink(file: FileModel): void {
    if (!file.downloadToken) return;
    const url = `${window.location.origin}/download/${file.downloadToken}`;
    navigator.clipboard.writeText(url).then(() => {
      this.copiedId = file.id;
      setTimeout(() => {
        if (this.copiedId === file.id) this.copiedId = null;
      }, 2000);
    }).catch(() => {
      // Fallback vieux navigateurs
      const ta = document.createElement('textarea');
      ta.value = url;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
      this.copiedId = file.id;
      setTimeout(() => { this.copiedId = null; }, 2000);
    });
  }
}
