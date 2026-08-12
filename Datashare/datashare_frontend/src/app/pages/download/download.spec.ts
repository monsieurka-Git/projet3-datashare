import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, Observable } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { DownloadComponent } from './download';
import { FileService, FileMetadata } from '../../services/file';

describe('DownloadComponent', () => {
  let component: DownloadComponent;
  let fixture: ComponentFixture<DownloadComponent>;
  let fileServiceMock: any;

  beforeEach(async () => {
    fileServiceMock = {
      getFileMetadata: vi.fn(() => of({} as FileMetadata)),
      downloadFile: vi.fn(() => of(new Blob(['data'], { type: 'application/pdf' })))
    };

    await TestBed.configureTestingModule({
      imports: [DownloadComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { params: { subscribe: (cb: any) => cb({ token: 'tok-1' }) } } },
        { provide: FileService, useValue: fileServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set status message when token is missing', () => {
    (component as any).route.params = { subscribe: (cb: any) => cb({}) };
    component.ngOnInit();
    expect(component.statusMessage).toBe('Aucun lien de téléchargement valide.');
  });

  it('should load metadata on init', () => {
    const meta: FileMetadata = { originalName: 'f.txt', size: 10, contentType: 'text/plain', expiresAt: new Date().toISOString() } as any;
    fileServiceMock.getFileMetadata = vi.fn(() => of(meta));
    (component as any).route.params = { subscribe: (cb: any) => cb({ token: 'tok-1' }) };

    component.ngOnInit();

    expect(component.fileMetadata).toEqual(meta);
    expect(component.statusMessage).toBe('');
  });

  it('should set error message on metadata load failure', () => {
    const error$ = new Observable<FileMetadata>((subscriber) => {
      subscriber.error({ status: 404, error: { error: 'Not found' } });
    });
    fileServiceMock.getFileMetadata = vi.fn(() => error$);
    (component as any).route.params = { subscribe: (cb: any) => cb({ token: 'tok-1' }) };

    component.ngOnInit();

    expect(component.fileMetadata).toBeNull();
    expect(component.statusMessage).not.toBe('');
  });

  it('should not download when no token', () => {
    component.token = '';
    component.fileMetadata = { originalName: 'f.txt' } as any;
    component.onDownload();
    expect(fileServiceMock.downloadFile).not.toHaveBeenCalled();
  });

  it('should not download when no metadata', () => {
    component.token = 'tok-1';
    component.fileMetadata = null;
    component.onDownload();
    expect(fileServiceMock.downloadFile).not.toHaveBeenCalled();
  });

  it('should require password for protected file', () => {
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: true } as any;
    component.password = '';
    component.onDownload();
    expect(component.statusMessage).toContain('protégé');
    expect(fileServiceMock.downloadFile).not.toHaveBeenCalled();
  });

  it('should download file successfully', () => {
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: false } as any;
    component.onDownload();
    expect(fileServiceMock.downloadFile).toHaveBeenCalled();
    expect(component.statusMessage).toContain('Téléchargement réussi');
  });

  it('should handle download error with password message', () => {
    fileServiceMock.downloadFile = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ error: { error: 'Mot de passe incorrect' } }); } } as any;
    });
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: true } as any;
    component.password = 'wrong';
    component.onDownload();
    expect(component.statusMessage).toContain('Mot de passe incorrect');
  });

  it('should handle download error with expired message', () => {
    fileServiceMock.downloadFile = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ error: { error: 'Ce lien a expiré' } }); } } as any;
    });
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: false } as any;
    component.onDownload();
    expect(component.statusMessage).toContain('expiré');
  });

  it('should handle download error with invalid message', () => {
    fileServiceMock.downloadFile = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ error: { error: 'Lien invalide' } }); } } as any;
    });
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: false } as any;
    component.onDownload();
    expect(component.statusMessage).toContain('invalide');
  });

  it('should handle generic download error', () => {
    fileServiceMock.downloadFile = vi.fn(() => {
      return { subscribe: (cb: any) => { cb.error({ error: { error: 'Erreur inconnue' } }); } } as any;
    });
    component.token = 'tok-1';
    component.fileMetadata = { originalName: 'f.txt', passwordProtected: false } as any;
    component.onDownload();
    expect(component.statusMessage).toContain('Erreur lors du téléchargement');
  });
});
