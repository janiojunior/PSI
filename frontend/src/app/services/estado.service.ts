import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Estado } from '../models/estado.model';

@Injectable({
  providedIn: 'root'
})
export class EstadoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/estados';

  // Sinais de estado interno
  private readonly _estados = signal<Estado[]>([]);
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);

  // Sinais somente-leitura expostos para os componentes
  readonly estados = this._estados.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  loadAll(): void {
    this._loading.set(true);
    this._error.set(null);

    this.http.get<Estado[]>(this.apiUrl).subscribe({
      next: (data) => {
        this._estados.set(data);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set('Erro ao carregar lista de estados do servidor');
        this._loading.set(false);
        console.error(err);
      }
    });
  }

  create(estado: Estado): Observable<Estado> {
    this._loading.set(true);
    return this.http.post<Estado>(this.apiUrl, estado).pipe(
      tap({
        next: (created) => {
          this._estados.update((current) => [...current, created]);
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao cadastrar estado');
          this._loading.set(false);
          console.error(err);
        }
      })
    );
  }

  getById(id: number): Observable<Estado> {
    return this.http.get<Estado>(`${this.apiUrl}/${id}`);
  }

  update(id: number, estado: Estado): Observable<Estado> {
    this._loading.set(true);
    return this.http.put<Estado>(`${this.apiUrl}/${id}`, estado).pipe(
      tap({
        next: (updated) => {
          this._estados.update((current) =>
            current.map((item) => (item.id === id ? updated : item))
          );
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao atualizar estado');
          this._loading.set(false);
          console.error(err);
        }
      })
    );
  }

  delete(id: number): Observable<void> {
    this._loading.set(true);
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap({
        next: () => {
          this._estados.update((current) => current.filter((e) => e.id !== id));
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao excluir estado');
          this._loading.set(false);
          console.error(err);
        }
      })
    );
  }
}

