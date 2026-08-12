// Routes DataShare — navigation simplifiée (maquettes Figma)
import { Routes } from '@angular/router';

import { LoginComponent } from './pages/login/login';
import { RegisterComponent } from './pages/register/register';
import { HomeComponent } from './pages/home/home';
import { UploadComponent } from './pages/upload/upload';
import { DownloadComponent } from './pages/download/download';
import { LandingComponent } from './pages/landing/landing.component';
import { AuthGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: 'welcome', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'upload', component: UploadComponent }, // US07 : accessible anonymes + connectés
  // Téléchargement via lien partagé uniquement (pas de menu)
  { path: 'download', component: DownloadComponent },
  { path: 'download/:token', component: DownloadComponent },
  { path: '', redirectTo: 'welcome', pathMatch: 'full' },
  { path: '**', redirectTo: 'welcome' }
];
