// Point d'entrée de l'application : bootstrap du composant racine standalone
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app';

// Les providers globaux (HttpClient, routing, intercepteur JWT) sont définis dans app.config.ts
bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
