// Tests unitaires de l'intercepteur d'authentification
import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { authInterceptor } from './auth-interceptor';
import { AuthService } from '../services/auth';
import { runInInjectionContext } from '@angular/core';

describe('authInterceptor', () => {
  let authServiceMock: { getToken: () => string | null };

  beforeEach(() => {
    // Mock du service d'authentification
    authServiceMock = {
      getToken: () => 'fake-token'
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock }
      ]
    });
  });

  it('should be created', () => {
    expect(authInterceptor).toBeTruthy();
  });

  it('should add Authorization header when token exists', () => {
    const req = new HttpRequest('GET', '/api/test');
    const next: HttpHandlerFn = (request: HttpRequest<unknown>): Observable<HttpEvent<unknown>> => {
      expect(request.headers.get('Authorization')).toBe('Bearer fake-token');
      return of({ type: 0, url: request.url } as HttpEvent<unknown>);
    };

    TestBed.runInInjectionContext(() => {
      authInterceptor(req, next).subscribe();
    });
  });

  it('should not add Authorization header when no token', () => {
    authServiceMock.getToken = () => null;

    const req = new HttpRequest('GET', '/api/test');
    const next: HttpHandlerFn = (request: HttpRequest<unknown>): Observable<HttpEvent<unknown>> => {
      expect(request.headers.has('Authorization')).toBeFalsy();
      return of({ type: 0, url: request.url } as HttpEvent<unknown>);
    };

    TestBed.runInInjectionContext(() => {
      authInterceptor(req, next).subscribe();
    });
  });
});
