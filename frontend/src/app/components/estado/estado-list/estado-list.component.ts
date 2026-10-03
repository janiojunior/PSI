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
import { EstadoService } from '../../../services/estado.service';
import { Estado } from '../../../models/estado.model';

@Component({
  selector: 'app-estado-list',
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
  templateUrl: './estado-list.component.html',
  styleUrl: './estado-list.component.scss'
})
export class EstadoListComponent implements OnInit {
  readonly estadoService = inject(EstadoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  displayedColumns: string[] = ['siglaPill', 'sigla', 'nome', 'id', 'acoes'];
  dataSource = new MatTableDataSource<Estado>([]);
  filtroTexto: string = '';

  @ViewChild(MatPaginator) set paginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  constructor() {
    // Sincroniza o Signal reativo de estados com a fonte de dados da MatTable
    effect(() => {
      const estados = this.estadoService.estados();
      this.dataSource.data = estados;
      this.aplicarFiltro();
    });
  }

  ngOnInit(): void {
    this.estadoService.loadAll();
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
      this.router.navigate(['/estados/editar', id]);
    }
  }

  excluir(estado: Estado): void {
    if (!estado.id) return;

    const confirmacao = window.confirm(`Deseja realmente excluir o estado ${estado.nome} (${estado.sigla})?`);
    if (!confirmacao) return;

    this.estadoService.delete(estado.id).subscribe({
      next: () => {
        this.snackBar.open(`Estado ${estado.sigla} excluído com sucesso!`, 'Fechar', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['psi-snackbar']
        });
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Erro ao excluir o estado selecionado.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }
}
