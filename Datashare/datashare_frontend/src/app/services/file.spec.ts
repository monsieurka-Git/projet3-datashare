import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FileService } from './file';

describe('FileService (US01 / US02 / US06)', () => {
  let service: FileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FileService]
    });
    service = TestBed.inject(FileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getFiles() appelle GET /api/files', () => {
    service.getFiles().subscribe(files => {
      expect(files.length).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/files') || r.url.endsWith('/files'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: '1', originalName: 'a.txt' }]);
  });

  it('uploadFile() construit un FormData correct', () => {
    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' });

    service.uploadFile(file, 5, 'secret1', 'tag1').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/upload') && !r.url.includes('anonymous'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    const fd = req.request.body as FormData;
    expect(fd.get('file')).toBeTruthy();
    expect(fd.get('expiresInDays')).toBe('5');
    expect(fd.get('password')).toBe('secret1');
    expect(fd.get('tags')).toBe('tag1');
    req.flush({ id: '1', downloadToken: 'tok', downloadUrl: '/download/tok' });
  });

  it('uploadAnonymous() appelle POST /api/files/upload/anonymous', () => {
    const file = new File(['x'], 'x.txt', { type: 'text/plain' });
    service.uploadAnonymous(file, 7).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/upload/anonymous'));
    expect(req.request.method).toBe('POST');
    req.flush({ id: '1', downloadToken: 'tok', downloadUrl: '/download/tok' });
  });

  it('deleteFile() appelle DELETE /api/files/info/{id}', () => {
    service.deleteFile('abc-id').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/info/abc-id'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('getFileMetadata() appelle GET /api/files/metadata/{token}', () => {
    service.getFileMetadata('tok-1').subscribe(meta => {
      expect(meta).toBeTruthy();
    });

    const req = httpMock.expectOne(r => r.url.includes('/files/metadata/tok-1'));
    expect(req.request.method).toBe('GET');
    req.flush({ originalName: 'f.txt', size: 10, contentType: 'text/plain' });
  });

  it('downloadFile() appelle POST /api/files/download/{token}', () => {
    service.downloadFile('tok-2', 'pwd').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/download/tok-2'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.get('password')).toBe('pwd');
    req.flush(new Blob(['data']));
  });

  it('downloadFile() sans mot de passe', () => {
    service.downloadFile('tok-3').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/download/tok-3'));
    expect(req.request.method).toBe('POST');
    expect(req.request.params.has('password')).toBe(false);
    req.flush(new Blob(['data']));
  });

  it('getUserFiles() appelle GET /api/files', () => {
    service.getUserFiles('user-1').subscribe(files => {
      expect(files.length).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/files') || r.url.endsWith('/files'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: '1', originalName: 'a.txt' }]);
  });

  it('updateTags() appelle PUT /api/files/{id}/tags', () => {
    service.updateTags('file-1', 'work,urgent').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/file-1/tags'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.params.get('tags')).toBe('work,urgent');
    req.flush({});
  });

  it('uploadFile() sans options optionnelles', () => {
    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' });

    service.uploadFile(file).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/files/upload') && !r.url.includes('anonymous'));
    expect(req.request.method).toBe('POST');
    const fd = req.request.body as FormData;
    expect(fd.get('file')).toBeTruthy();
    expect(fd.get('expiresInDays')).toBeNull();
    expect(fd.get('password')).toBeNull();
    expect(fd.get('tags')).toBeNull();
    req.flush({ id: '1', downloadToken: 'tok', downloadUrl: '/download/tok' });
  });

  it('uploadAnonymous() sans options optionnelles', () => {
    const file = new File(['x'], 'x.txt', { type: 'text/plain' });
    service.uploadAnonymous(file).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/upload/anonymous'));
    expect(req.request.method).toBe('POST');
    const fd = req.request.body as FormData;
    expect(fd.get('file')).toBeTruthy();
    expect(fd.get('expiresInDays')).toBeNull();
    expect(fd.get('password')).toBeNull();
    req.flush({ id: '1', downloadToken: 'tok', downloadUrl: '/download/tok' });
  });
});
