import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth';

describe('AuthService (US03 / US04)', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login() envoie POST /api/auth/login', () => {
    const body = { email: 'a@b.com', password: 'password1' };
    const mockResp = { token: 'jwt', type: 'Bearer', expiresIn: 3600, userId: 'u1', email: 'a@b.com' };

    service.login(body).subscribe(res => {
      expect(res.token).toBe('jwt');
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/auth/login') || r.url.includes('/auth/login'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(mockResp);
  });

  it('register() envoie POST /api/auth/register', () => {
    const body = { email: 'new@b.com', password: 'password1' };

    service.register(body).subscribe(msg => {
      expect(typeof msg === 'string' || msg != null).toBe(true);
    });

    const req = httpMock.expectOne(r => r.url.includes('/register'));
    expect(req.request.method).toBe('POST');
    req.flush('Utilisateur créé avec succès');
  });

  it('saveToken / getToken / logout gèrent le localStorage', () => {
    service.saveToken('abc-token');
    expect(service.getToken()).toBe('abc-token');
    service.logout();
    expect(service.getToken()).toBeNull();
  });

  it('isAuthenticated() retourne true si un token existe', () => {
    expect(service.isAuthenticated()).toBe(false);
    service.saveToken('tok');
    expect(service.isAuthenticated()).toBe(true);
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('saveUserId / getCurrentUserId', () => {
    service.saveUserId('user-uuid');
    expect(service.getCurrentUserId()).toBe('user-uuid');
    service.logout();
    expect(service.getCurrentUserId()).toBeNull();
  });
});
