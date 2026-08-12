// Configuration globale de l'application Angular (standalone)

import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Routing standalone
    provideRouter(routes),

    // HttpClient + Interceptor JWT
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
