// Tests unitaires du composant Navbar
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { NavbarComponent } from './navbar';
import { AuthService } from '../../services/auth';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let authServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have title DataShare', () => {
    expect(component.title).toBe('DataShare');
  });

  it('should logout and navigate to login', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;

    component.onLogout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
