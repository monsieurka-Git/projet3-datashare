// Composant d'en-tête : logo DataShare + actions (Documentation, Se connecter, Importer des fichiers)

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone : true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  // Affiche ou non le bouton "Se connecter"
  @Input() showLogin = false;

  // Affiche ou non le bouton "Importer des fichiers"
  @Input() showImport = false;

  // Affiche ou non le lien "Documentation"
  @Input() showDocs = true;
}
