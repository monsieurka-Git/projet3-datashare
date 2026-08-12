import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { UploadComponent } from './upload';
import { FileService } from '../../services/file';
import { AuthService } from '../../services/auth';

describe('UploadComponent', () => {
  let component: UploadComponent;
  let fixture: ComponentFixture<UploadComponent>;
  let fileServiceMock: any;
  let authServiceMock: any;

  beforeEach(async () => {
    fileServiceMock = {
      uploadFile: vi.fn(() => of({ downloadToken: 'tok-1', downloadUrl: '/download/tok-1' })),
      uploadAnonymous: vi.fn(() => of({ downloadToken: 'tok-1', downloadUrl: '/download/tok-1' }))
    };
    authServiceMock = {
      isAuthenticated: vi.fn(() => false)
    };

    await TestBed.configureTestingModule({
      imports: [UploadComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { params: { subscribe: () => {} } } },
        { provide: FileService, useValue: fileServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default form values', () => {
    expect(component.selectedFile).toBeNull();
    expect(component.expirationDays).toBe(7);
    expect(component.password).toBe('');
    expect(component.tags).toBe('');
    expect(component.selectedFileName).toBe('');
    expect(component.statusMessage).toBe('');
  });

  it('should set error message', () => {
    component.statusMessage = 'Erreur';
    expect(component.statusMessage).toBe('Erreur');
  });

  it('should clear status message', () => {
    component.statusMessage = 'Erreur';
    component.statusMessage = '';
    expect(component.statusMessage).toBe('');
  });

  it('should not upload if no file selected', () => {
    component.selectedFile = null;
    component.onUpload();
    expect(component.statusMessage).toContain('Veuillez choisir un fichier');
  });

  it('should select a valid file', () => {
    const file = new File(['content'], 'test.txt', { type: 'text/plain' });
    const event = { target: { files: [file] } } as any;

    component.onFileSelected(event);

    expect(component.selectedFile).toBe(file);
    expect(component.selectedFileName).toBe('test.txt');
    expect(component.selectedFileSize).toContain('MB');
  });

  it('should reject forbidden file extension', () => {
    const file = new File(['content'], 'malware.exe', { type: 'application/octet-stream' });
    const event = { target: { files: [file] } } as any;

    component.onFileSelected(event);

    expect(component.selectedFile).toBeNull();
    expect(component.statusMessage).toContain('Type de fichier interdit');
  });

  it('should upload anonymously when not authenticated', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(fileServiceMock.uploadAnonymous).toHaveBeenCalled();
    expect(component.downloadToken).toBe('tok-1');
    expect(component.statusMessage).toContain('succès');
  });

  it('should upload with auth when authenticated', () => {
    authServiceMock.isAuthenticated = vi.fn(() => true);
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(fileServiceMock.uploadFile).toHaveBeenCalled();
    expect(component.downloadToken).toBe('tok-1');
  });

  it('should reject password shorter than 6 chars', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.password = 'abc';
    component.onUpload();

    expect(component.statusMessage).toContain('au moins 6 caractères');
  });

  it('should reject non-alphanumeric password', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.password = 'abc@123';
    component.onUpload();

    expect(component.statusMessage).toContain('alphanumérique');
  });

  it('should reject invalid expiration days', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.expirationDays = 10;
    component.onUpload();

    expect(component.statusMessage).toContain('1 et 7 jours');
  });

  it('should handle upload error', () => {
    fileServiceMock.uploadAnonymous = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ status: 500, error: { error: 'Server error' } }); } } as any;
    });
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(component.statusMessage).toContain('Server error');
  });

  it('should handle upload timeout error', () => {
    fileServiceMock.uploadAnonymous = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ name: 'TimeoutError' }); } } as any;
    });
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(component.statusMessage).toContain('expiré');
  });

  it('should handle 401 error', () => {
    fileServiceMock.uploadAnonymous = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ status: 401 }); } } as any;
    });
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(component.statusMessage).toContain('Session expirée');
  });

  it('should handle 403 error', () => {
    fileServiceMock.uploadAnonymous = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ status: 403 }); } } as any;
    });
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(component.statusMessage).toContain('Accès refusé');
  });

  it('should handle status 0 error', () => {
    fileServiceMock.uploadAnonymous = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ status: 0 }); } } as any;
    });
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.onUpload();

    expect(component.statusMessage).toContain('Serveur injoignable');
  });

  it('should copy link when downloadUrl exists', async () => {
    component.downloadUrl = 'http://localhost:4200/download/tok-1';
    const writeTextSpy = vi.fn().mockResolvedValue(undefined);
    Object.defineProperty(navigator, 'clipboard', { value: { writeText: writeTextSpy }, configurable: true });
    Object.defineProperty(window, 'isSecureContext', { value: true, configurable: true });

    await component.copyLink();

    expect(writeTextSpy).toHaveBeenCalledWith('http://localhost:4200/download/tok-1');
    expect(component.linkCopied).toBe(true);
  });

  it('should not copy link when no downloadUrl', async () => {
    component.downloadUrl = null;
    await component.copyLink();
    expect(component.linkCopied).toBe(false);
  });
});
