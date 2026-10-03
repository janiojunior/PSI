import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EstadoService } from '../../../services/estado.service';
import { Estado } from '../../../models/estado.model';

@Component({
  selector: 'app-estado-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './estado-form.component.html',
  styleUrl: './estado-form.component.scss'
})
export class EstadoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  form!: FormGroup;
  isEditMode = false;
  estadoId: number | null = null;
  carregando = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.form = this.fb.group({
      sigla: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.estadoId = Number(idParam);
      this.carregarEstado(this.estadoId);
    }
  }

  private carregarEstado(id: number): void {
    this.carregando = true;
    this.estadoService.getById(id).subscribe({
      next: (estado) => {
        this.form.patchValue({
          sigla: estado.sigla,
          nome: estado.nome
        });
        this.carregando = false;
      },
      error: (err) => {
        console.error(err);
        this.carregando = false;
        this.snackBar.open('Não foi possível carregar os dados do estado.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        this.router.navigate(['/estados']);
      }
    });
  }

  onSiglaInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase();
    this.form.get('sigla')?.setValue(input.value, { emitEvent: false });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dados: Estado = {
      sigla: this.form.value.sigla.toUpperCase(),
      nome: this.form.value.nome
    };

    if (this.isEditMode && this.estadoId) {
      this.estadoService.update(this.estadoId, { ...dados, id: this.estadoId }).subscribe({
        next: () => {
          this.snackBar.open('Estado atualizado com sucesso!', 'Fechar', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['psi-snackbar']
          });
          this.router.navigate(['/estados']);
        },
        error: (err) => {
          console.error(err);
          this.snackBar.open('Erro ao atualizar o estado. Verifique os dados.', 'Fechar', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        }
      });
    } else {
      this.estadoService.create(dados).subscribe({
        next: () => {
          this.snackBar.open('Estado cadastrado com sucesso!', 'Fechar', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['psi-snackbar']
          });
          this.router.navigate(['/estados']);
        },
        error: (err) => {
          console.error(err);
          this.snackBar.open('Erro ao cadastrar o estado. Verifique se a sigla já existe.', 'Fechar', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        }
      });
    }
  }

  excluir(): void {
    if (!this.estadoId) return;

    const confirmacao = window.confirm(`Deseja realmente excluir este estado?`);
    if (!confirmacao) return;

    this.estadoService.delete(this.estadoId).subscribe({
      next: () => {
        this.snackBar.open('Estado excluído com sucesso!', 'Fechar', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['psi-snackbar']
        });
        this.router.navigate(['/estados']);
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Erro ao excluir o estado.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/estados']);
  }
}
