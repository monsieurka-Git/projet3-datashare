import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { HomeComponent } from './home';
import { AuthService } from '../../services/auth';
import { FileService } from '../../services/file';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let fileServiceMock: any;

  beforeEach(async () => {
    fileServiceMock = {
      getFiles: vi.fn(() => of([])),
      deleteFile: vi.fn(() => of(void 0))
    };

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        { provide: ActivatedRoute, useValue: {} },
        { provide: AuthService, useValue: { saveToken: () => {}, saveUserId: () => {}, getToken: () => null, logout: () => {}, isAuthenticated: () => false } },
        { provide: FileService, useValue: fileServiceMock },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter displayed files by active tab', () => {
    const futureDate = new Date(Date.now() + 86400000).toISOString();
    const pastDate = new Date(Date.now() - 86400000).toISOString();
    component.files = [
      { id: '1', originalName: 'a.txt', expiresAt: futureDate, tags: 'work' } as any,
      { id: '2', originalName: 'b.txt', expiresAt: pastDate, tags: 'personal' } as any
    ];

    component.activeTab = 'Actifs';
    expect(component.displayedFiles.length).toBe(1);
    expect(component.displayedFiles[0].id).toBe('1');

    component.activeTab = 'Expirés';
    expect(component.displayedFiles.length).toBe(1);
    expect(component.displayedFiles[0].id).toBe('2');

    component.activeTab = 'Tous';
    expect(component.displayedFiles.length).toBe(2);
  });

  it('should filter files by tag', () => {
    component.files = [
      { id: '1', originalName: 'a.txt', tags: 'work,urgent' } as any,
      { id: '2', originalName: 'b.txt', tags: 'personal' } as any
    ];

    component.tagFilter = 'work';
    expect(component.displayedFiles.length).toBe(1);
    expect(component.displayedFiles[0].id).toBe('1');
  });

  it('should return false for isExpired when no expiresAt', () => {
    const file = { id: '1', originalName: 'a.txt' } as any;
    expect(component.isExpired(file)).toBe(false);
  });

  it('should return true for isExpired when expired', () => {
    const file = { id: '1', originalName: 'a.txt', expiresAt: new Date(Date.now() - 1000).toISOString() } as any;
    expect(component.isExpired(file)).toBe(true);
  });

  it('should refresh files', () => {
    let loaded = false;
    (component as any).loadFiles = () => { loaded = true; };
    component.refreshFiles();
    expect(loaded).toBe(true);
  });

  it('should select tab', () => {
    component.selectTab('Expirés');
    expect(component.activeTab).toBe('Expirés');
  });

  it('should return tag list from comma-separated string', () => {
    const file = { id: '1', tags: 'work, urgent,  perso' } as any;
    expect(component.tagList(file)).toEqual(['work', 'urgent', 'perso']);
  });

  it('should return empty tag list when no tags', () => {
    const file = { id: '1', tags: '' } as any;
    expect(component.tagList(file)).toEqual([]);
  });

  it('should open delete modal', () => {
    const file = { id: '1', originalName: 'a.txt' } as any;
    component.confirmDelete(file);
    expect(component.fileToDelete).toBe(file);
    expect(component.showDeleteModal).toBe(true);
  });

  it('should cancel delete modal', () => {
    component.fileToDelete = { id: '1' } as any;
    component.showDeleteModal = true;
    component.onDeleteCancel();
    expect(component.showDeleteModal).toBe(false);
    expect(component.fileToDelete).toBeNull();
  });

  it('should logout and navigate to login', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;
    const logoutSpy = vi.fn();
    (component as any).authService.logout = logoutSpy;

    component.onLogout();

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should navigate to download page on access', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;
    const file = { id: '1', downloadToken: 'tok-1' } as any;

    component.onAccess(file);

    expect(navigateSpy).toHaveBeenCalledWith(['/download', 'tok-1']);
  });

  it('should not navigate when no download token', () => {
    const navigateSpy = vi.fn();
    (component as any).router = { navigate: navigateSpy } as any;
    const file = { id: '1', downloadToken: null } as any;

    component.onAccess(file);

    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('should load files and normalize array response', () => {
    const files = [{ id: '1', originalName: 'a.txt' }] as any;
    fileServiceMock.getFiles.mockReturnValue(of(files));
    (component as any).loadFiles();
    expect(component.files).toEqual(files);
    expect(component.isLoading).toBe(false);
    expect(component.loadError).toBeNull();
  });

  it('should load files and normalize content response', () => {
    const files = [{ id: '1', originalName: 'a.txt' }] as any;
    fileServiceMock.getFiles.mockReturnValue(of({ content: files }));
    (component as any).loadFiles();
    expect(component.files).toEqual(files);
    expect(component.isLoading).toBe(false);
  });

  it('should handle empty response', () => {
    fileServiceMock.getFiles.mockReturnValue(of(null));
    (component as any).loadFiles();
    expect(component.files).toEqual([]);
    expect(component.isLoading).toBe(false);
  });

  it('should handle load error with 401', () => {
    fileServiceMock.getFiles.mockReturnValue(throwError(() => ({ status: 401 })));
    (component as any).loadFiles();
    expect(component.loadError).toBe('Session expirée ou non autorisée. Reconnectez-vous.');
  });

  it('should handle load error with status 0', () => {
    fileServiceMock.getFiles.mockReturnValue(throwError(() => ({ status: 0 })));
    (component as any).loadFiles();
    expect(component.loadError).toContain('backend arrêté');
  });

  it('should handle load error generic', () => {
    fileServiceMock.getFiles.mockReturnValue(throwError(() => ({ error: { error: 'Erreur API' } })));
    (component as any).loadFiles();
    expect(component.loadError).toBe('Erreur API');
  });

  it('should delete file on confirm', () => {
    component.fileToDelete = { id: 'file-1', originalName: 'a.txt' } as any;
    component.showDeleteModal = true;
    component.onDeleteConfirm();

    expect(component.showDeleteModal).toBe(false);
    expect(component.fileToDelete).toBeNull();
    expect(fileServiceMock.deleteFile).toHaveBeenCalledWith('file-1');
  });

  it('should not delete when no fileToDelete', () => {
    component.fileToDelete = null;
    component.onDeleteConfirm();
    expect(fileServiceMock.deleteFile).not.toHaveBeenCalled();
  });

  it('should restore files on delete failure', () => {
    const previous = [{ id: 'file-1', originalName: 'a.txt' }] as any;
    component.files = [...previous];
    component.fileToDelete = previous[0];
    component.showDeleteModal = true;
    fileServiceMock.deleteFile.mockReturnValue(throwError(() => ({ error: { error: 'Suppression impossible' } })));

    component.onDeleteConfirm();

    expect(component.files).toEqual(previous);
    expect(component.isDeleting).toBe(false);
  });

  it('should copy link with clipboard API', () => {
    const file = { id: '1', downloadToken: 'tok-1' } as any;
    const writeTextSpy = vi.fn().mockResolvedValue(undefined);
    Object.defineProperty(navigator, 'clipboard', { value: { writeText: writeTextSpy }, configurable: true });

    component.copyLink(file);

    expect(writeTextSpy).toHaveBeenCalledWith(`${window.location.origin}/download/tok-1`);
  });

  it('should not copy link when no download token', () => {
    const file = { id: '1', downloadToken: null } as any;
    const writeTextSpy = vi.fn();
    Object.defineProperty(navigator, 'clipboard', { value: { writeText: writeTextSpy }, configurable: true });

    component.copyLink(file);
    expect(writeTextSpy).not.toHaveBeenCalled();
  });
});
