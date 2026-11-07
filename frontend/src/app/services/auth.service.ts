import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthRequest, AuthResponse, NomePerfil, UsuarioAutenticado } from '../models/auth.model';

const STORAGE_TOKEN_KEY = 'psi_auth_token';
const STORAGE_USER_KEY = 'psi_auth_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = 'http://localhost:8080/auth';

  private readonly _token = signal<string | null>(null);
  private readonly _usuario = signal<UsuarioAutenticado | null>(null);
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  readonly isAuthenticated = computed<boolean>(() => {
    const token = this._token();
    return !!token && !this.isTokenExpired(token);
  });

  readonly iniciaisUsuario = computed<string>(() => {
    const nome = this._usuario()?.nome?.trim();
    if (!nome) {
      return 'US';
    }
    const partes = nome.split(/\s+/).filter((p) => !p.endsWith('.'));
    if (partes.length === 0) {
      return nome.substring(0, 2).toUpperCase();
    }
    if (partes.length === 1) {
      return partes[0].substring(0, 2).toUpperCase();
    }
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
  });

  readonly perfilPrincipal = computed<string>(() => {
    const perfis = this._usuario()?.perfis;
    if (!perfis || perfis.length === 0) {
      return 'Usuário Autenticado';
    }
    return perfis.map((p) => p.nome).join(' · ');
  });

  constructor() {
    this.restaurarSessao();
  }

  login(credenciais: AuthRequest): Observable<AuthResponse> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credenciais).pipe(
      tap({
        next: (res) => {
          this.salvarSessao(res.token, res.usuario);
          this._loading.set(false);
        },
        error: (err) => {
          const msg =
            err?.error?.message ||
            (err?.status === 401
              ? 'E-mail ou senha inválidos, ou usuário inativo.'
              : 'Não foi possível autenticar no servidor.');
          this._error.set(msg);
          this._loading.set(false);
        }
      })
    );
  }

  carregarPerfilAtual(): Observable<UsuarioAutenticado> {
    return this.http.get<UsuarioAutenticado>(`${this.apiUrl}/me`).pipe(
      tap({
        next: (usuario) => {
          this._usuario.set(usuario);
          if (typeof localStorage !== 'undefined') {
            localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(usuario));
          }
        }
      })
    );
  }

  logout(redirecionarParaLogin = true): void {
    this._token.set(null);
    this._usuario.set(null);
    this._error.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_TOKEN_KEY);
      localStorage.removeItem(STORAGE_USER_KEY);
    }
    if (redirecionarParaLogin) {
      this.router.navigate(['/login']);
    }
  }

  hasAnyRole(rolesPermitidas: NomePerfil[]): boolean {
    if (!rolesPermitidas || rolesPermitidas.length === 0) {
      return true;
    }
    const token = this._token();
    const groupsNoToken = this.extrairGroupsDoToken(token);
    if (groupsNoToken.some((g) => rolesPermitidas.includes(g as NomePerfil))) {
      return true;
    }
    const perfisUsuario = this._usuario()?.perfis ?? [];
    const mapaIdParaRole: Record<number, NomePerfil> = {
      1: 'ADMINISTRADOR',
      2: 'ATENDENTE',
      3: 'PACIENTE',
      4: 'PSICOLOGO'
    };
    return perfisUsuario.some((p) => rolesPermitidas.includes(mapaIdParaRole[p.id]));
  }

  private salvarSessao(token: string, usuario: UsuarioAutenticado): void {
    this._token.set(token);
    this._usuario.set(usuario);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(STORAGE_TOKEN_KEY, token);
      localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(usuario));
    }
  }

  private restaurarSessao(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    const token = localStorage.getItem(STORAGE_TOKEN_KEY);
    const rawUser = localStorage.getItem(STORAGE_USER_KEY);
    if (!token || this.isTokenExpired(token)) {
      localStorage.removeItem(STORAGE_TOKEN_KEY);
      localStorage.removeItem(STORAGE_USER_KEY);
      return;
    }
    this._token.set(token);
    if (rawUser) {
      try {
        this._usuario.set(JSON.parse(rawUser) as UsuarioAutenticado);
      } catch {
        localStorage.removeItem(STORAGE_USER_KEY);
      }
    }
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.decodificarPayloadJwt(token);
    if (!payload || typeof payload['exp'] !== 'number') {
      return false;
    }
    const agoraSegundos = Math.floor(Date.now() / 1000);
    return payload['exp'] <= agoraSegundos;
  }

  private extrairGroupsDoToken(token: string | null): string[] {
    if (!token) {
      return [];
    }
    const payload = this.decodificarPayloadJwt(token);
    const groups = payload?.['groups'];
    return Array.isArray(groups) ? groups.filter((g): g is string => typeof g === 'string') : [];
  }

  private decodificarPayloadJwt(token: string): Record<string, unknown> | null {
    try {
      const partes = token.split('.');
      if (partes.length !== 3) {
        return null;
      }
      const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
