// Carte de fichier : affiche un fichier avec ses informations
// Composant standalone : peut être importé directement sans NgModule

import { Component } from '@angular/core';

@Component({
  selector: 'app-file-card',
  standalone: true,
  imports: [],
  templateUrl: './file-card.html',
  styleUrl: './file-card.css',
})
export class FileCard {}