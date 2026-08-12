import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { Share } from './share';
import { FileService } from '../../services/file';
import { ShareService } from '../../services/share';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('Share', () => {
  let component: Share;
  let fixture: ComponentFixture<Share>;
  let fileServiceMock: any;
  let shareServiceMock: any;

  beforeEach(async () => {
    fileServiceMock = {
      uploadFile: vi.fn(() => of({ id: 'file-123' }))
    };
    shareServiceMock = {
      shareFile: vi.fn(() => of({ message: 'Fichier partagé', downloadToken: 'tok-1' }))
    };

    await TestBed.configureTestingModule({
      imports: [Share],
      providers: [
        { provide: FileService, useValue: fileServiceMock },
        { provide: ShareService, useValue: shareServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Share);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select file and set file name', () => {
    const file = new File(['content'], 'test.txt', { type: 'text/plain' });
    const event = { target: { files: [file] } } as any;

    component.onFileSelected(event);

    expect(component.selectedFile).toBe(file);
    expect(component.selectedFileName).toBe('test.txt');
    expect(component.statusMessage).toBe('');
    expect(component.shareDownloadUrl).toBeNull();
  });

  it('should show error when no file selected', () => {
    component.selectedFile = null;
    component.recipientEmail = 'test@test.com';
    component.onShare();
    expect(component.statusMessage).toBe('Veuillez sélectionner un fichier.');
  });

  it('should show error when no recipient email', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.recipientEmail = '';
    component.onShare();
    expect(component.statusMessage).toBe('Veuillez saisir l\'email du destinataire.');
  });

  it('should upload and share file successfully', () => {
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.recipientEmail = 'dest@test.com';
    component.onShare();

    expect(fileServiceMock.uploadFile).toHaveBeenCalled();
    expect(component.statusMessage).toContain('Fichier partagé');
    expect(component.shareDownloadUrl).toContain('/download/tok-1');
  });

  it('should handle upload error', () => {
    fileServiceMock.uploadFile = () => {
      return { subscribe: (cb: any) => { cb.error({ error: { error: 'Upload failed' } }); } } as any;
    };
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.recipientEmail = 'dest@test.com';
    component.onShare();

    expect(component.statusMessage).toBe('Upload failed');
  });

  it('should handle missing file id', () => {
    fileServiceMock.uploadFile = () => of({});
    component.selectedFile = new File(['x'], 'x.txt', { type: 'text/plain' });
    component.recipientEmail = 'dest@test.com';
    component.onShare();

    expect(component.statusMessage).toBe('Upload OK mais identifiant fichier manquant.');
  });
});
