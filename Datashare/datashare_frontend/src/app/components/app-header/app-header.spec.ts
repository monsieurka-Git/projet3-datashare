import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { AppHeaderBar } from './app-header';
import { AuthService } from '../../services/auth';

describe('AppHeaderBar', () => {
  let component: AppHeaderBar;
  let fixture: ComponentFixture<AppHeaderBar>;
  let authServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      isAuthenticated: vi.fn(() => false),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AppHeaderBar],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeaderBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have showAddFiles default to false', () => {
    expect(component.showAddFiles).toBe(false);
  });

  it('should return isLoggedIn false when not authenticated', () => {
    authServiceMock.isAuthenticated.mockReturnValue(false);
    expect(component.isLoggedIn).toBe(false);
  });

  it('should return isLoggedIn true when authenticated', () => {
    authServiceMock.isAuthenticated.mockReturnValue(true);
    expect(component.isLoggedIn).toBe(true);
  });

  it('should logout and navigate to login', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;

    component.onLogout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});