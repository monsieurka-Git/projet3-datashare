// Sidebar de navigation (Mon espace, Téléverser, Partager, Télécharger)
// Composant standalone : peut être importé directement sans NgModule

import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgFor } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, NgFor],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent {
  // Liste des liens de navigation
  links = [
    { label: 'Mon espace', route: '/home' },
    { label: 'Téléverser', route: '/upload' },
    { label: 'Partager', route: '/share' },
    { label: 'Télécharger', route: '/download' }
  ];
}