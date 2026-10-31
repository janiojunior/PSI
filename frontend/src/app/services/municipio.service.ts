import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Municipio } from '../models/municipio.model';

@Injectable({
  providedIn: 'root'
})
export class MunicipioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/municipios';

  // Sinais de estado interno
  private readonly _municipios = signal<Municipio[]>([]);
  private readonly _loading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);

  // Sinais somente-leitura expostos para os componentes
  readonly municipios = this._municipios.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  loadAll(): void {
    this._loading.set(true);
    this._error.set(null);

    this.http.get<Municipio[]>(this.apiUrl).subscribe({
      next: (data) => {
        this._municipios.set(data);
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set('Erro ao carregar lista de municípios do servidor');
        this._loading.set(false);
        console.error(err);
      }
    });
  }

  create(municipio: Municipio): Observable<Municipio> {
    this._loading.set(true);
    return this.http.post<Municipio>(this.apiUrl, municipio).pipe(
      tap({
        next: (created) => {
          this._municipios.update((current) => [...current, created]);
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao cadastrar município');
          this._loading.set(false);
          console.error(err);
        }
      })
    );
  }

  getById(id: number): Observable<Municipio> {
    return this.http.get<Municipio>(`${this.apiUrl}/${id}`);
  }

  update(id: number, municipio: Municipio): Observable<Municipio> {
    this._loading.set(true);
    return this.http.put<Municipio>(`${this.apiUrl}/${id}`, municipio).pipe(
      tap({
        next: (updated) => {
          this._municipios.update((current) =>
            current.map((item) => (item.id === id ? updated : item))
          );
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao atualizar município');
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
          this._municipios.update((current) => current.filter((m) => m.id !== id));
          this._loading.set(false);
        },
        error: (err) => {
          this._error.set('Erro ao excluir município');
          this._loading.set(false);
          console.error(err);
        }
      })
    );
  }
}
