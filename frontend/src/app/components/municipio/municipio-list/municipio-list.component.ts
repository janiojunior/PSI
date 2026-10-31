import { Component, OnInit, ViewChild, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { MunicipioService } from '../../../services/municipio.service';
import { Municipio } from '../../../models/municipio.model';

@Component({
  selector: 'app-municipio-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './municipio-list.component.html',
  styleUrl: './municipio-list.component.scss'
})
export class MunicipioListComponent implements OnInit {
  readonly municipioService = inject(MunicipioService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  displayedColumns: string[] = ['ufPill', 'nome', 'estado', 'id', 'acoes'];
  dataSource = new MatTableDataSource<Municipio>([]);
  filtroTexto: string = '';

  @ViewChild(MatPaginator) set paginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  constructor() {
    this.dataSource.filterPredicate = (data: Municipio, filter: string): boolean => {
      const termo = filter.trim().toLowerCase();
      const nomeMunicipio = (data.nome ?? '').toLowerCase();
      const nomeEstado = (data.estado?.nome ?? '').toLowerCase();
      const siglaEstado = (data.estado?.sigla ?? '').toLowerCase();
      return (
        nomeMunicipio.includes(termo) ||
        nomeEstado.includes(termo) ||
        siglaEstado.includes(termo)
      );
    };

    effect(() => {
      const municipios = this.municipioService.municipios();
      this.dataSource.data = municipios;
      this.aplicarFiltro();
    });
  }

  ngOnInit(): void {
    this.municipioService.loadAll();
  }

  aplicarFiltro(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.dataSource.filter = termo;
  }

  limparFiltro(): void {
    this.filtroTexto = '';
    this.dataSource.filter = '';
  }

  editar(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/municipios/editar', id]);
    }
  }

  excluir(municipio: Municipio): void {
    if (!municipio.id) return;

    const confirmacao = window.confirm(
      `Deseja realmente excluir o município ${municipio.nome} (${municipio.estado?.sigla ?? ''})?`
    );
    if (!confirmacao) return;

    this.municipioService.delete(municipio.id).subscribe({
      next: () => {
        this.snackBar.open(`Município ${municipio.nome} excluído com sucesso!`, 'Fechar', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['psi-snackbar']
        });
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Erro ao excluir o município selecionado.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }
}
