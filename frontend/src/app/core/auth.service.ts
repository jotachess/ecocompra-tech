import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { Router } from '@angular/router';
import { API_URL, Sesion } from './models';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private session = signal<Sesion | null>(null);
  private expiry = 0;
  private timer?: ReturnType<typeof setTimeout>;
  user = computed(() => this.session()?.username ?? '');
  role = computed(() => (this.session()?.roles.includes('ADMIN') ? 'Administrador' : 'Vendedor'));
  admin = computed(() => this.session()?.roles.includes('ADMIN') ?? false);
  authenticated = computed(() => !!this.session());
  valid() {
    return this.session() !== null && Date.now() < this.expiry;
  }
  token() {
    return this.valid() ? this.session()!.accessToken : null;
  }
  login(username: string, password: string) {
    return this.http.post<Sesion>(`${API_URL}/auth/login`, { username, password }).pipe(
      tap((session) => {
        this.logout();
        this.session.set(session);
        this.expiry = Date.now() + session.expiresIn * 1000;
        this.timer = setTimeout(() => {
          this.logout();
          void this.router.navigate(['/login']);
        }, session.expiresIn * 1000);
      }),
    );
  }
  logout() {
    if (this.timer) clearTimeout(this.timer);
    this.session.set(null);
    this.expiry = 0;
  }
}
