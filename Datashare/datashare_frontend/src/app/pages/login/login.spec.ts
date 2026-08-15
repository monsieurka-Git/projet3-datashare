// Tests unitaires du composant de connexion
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { LoginComponent } from './login';
import { AuthService } from '../../services/auth';
import { describe, it, expect, vi } from 'vitest';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: any;

  beforeEach(async () => {
    const authSpy = {
      login: vi.fn(),
      saveToken: vi.fn(),
      saveUserId: vi.fn(),
      getToken: vi.fn(),
      isAuthenticated: vi.fn(() => false)
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, HttpClientTestingModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authServiceMock = TestBed.inject(AuthService) as any;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.form.email).toBe('');
    expect(component.form.password).toBe('');
    expect(component.errorMessage).toBe('');
    expect(component.isSubmitting).toBe(false);
  });

  it('should call onSubmit and navigate on successful login', () => {
    const mockResponse = {
      token: 'fake-jwt',
      type: 'Bearer',
      expiresIn: 3600,
      userId: 'user-123',
      email: 'user@test.com'
    };

    authServiceMock.login.mockReturnValue(of(mockResponse) as any);
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;

    component.form.email = 'user@test.com';
    component.form.password = 'password';
    component.onSubmit();

    expect(authServiceMock.login).toHaveBeenCalledWith(component.form);
    expect(authServiceMock.saveToken).toHaveBeenCalledWith('fake-jwt');
    expect(authServiceMock.saveUserId).toHaveBeenCalledWith('user-123');
    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
    expect(component.isSubmitting).toBe(false);
  });

  it('should set error message on login failure', () => {
    const errorResponse = { status: 401, error: { error: 'Invalid credentials' } };
    authServiceMock.login.mockReturnValue(
      { subscribe: (cb: any) => { cb.error(errorResponse); return { unsubscribe: () => {} }; } } as any
    );

    component.form.email = 'bad@test.com';
    component.form.password = 'wrong';
    component.onSubmit();

    expect(component.errorMessage).toBe('Identifiants incorrects. Veuillez réessayer.');
    expect(component.isSubmitting).toBe(false);
  });

  it('should not submit if already submitting', () => {
    component.isSubmitting = true;
    component.onSubmit();

    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('should navigate to register page', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;

    component.goToRegister();

    expect(navigateSpy).toHaveBeenCalledWith(['/register']);
  });
});
