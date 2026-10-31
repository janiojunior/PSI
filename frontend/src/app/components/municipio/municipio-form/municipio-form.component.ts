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
import { MunicipioService } from '../../../services/municipio.service';
import { EstadoService } from '../../../services/estado.service';
import { Municipio } from '../../../models/municipio.model';

@Component({
  selector: 'app-municipio-form',
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
  templateUrl: './municipio-form.component.html',
  styleUrl: './municipio-form.component.scss'
})
export class MunicipioFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly municipioService = inject(MunicipioService);
  readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  form!: FormGroup;
  isEditMode = false;
  municipioId: number | null = null;
  carregando = false;

  ngOnInit(): void {
    this.initForm();
    this.estadoService.loadAll();
    this.checkEditMode();
  }

  private initForm(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(60)]],
      estadoId: [null, [Validators.required]]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.municipioId = Number(idParam);
      this.carregarMunicipio(this.municipioId);
    }
  }

  private carregarMunicipio(id: number): void {
    this.carregando = true;
    this.municipioService.getById(id).subscribe({
      next: (municipio) => {
        this.form.patchValue({
          nome: municipio.nome,
          estadoId: municipio.estado?.id ?? null
        });
        this.carregando = false;
      },
      error: (err) => {
        console.error(err);
        this.carregando = false;
        this.snackBar.open('Não foi possível carregar os dados do município.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        this.router.navigate(['/municipios']);
      }
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const estadoIdSelecionado = Number(this.form.value.estadoId);
    const estadoSelecionado = this.estadoService
      .estados()
      .find((e) => e.id === estadoIdSelecionado);

    const dados: Municipio = {
      nome: this.form.value.nome.trim(),
      estado: estadoSelecionado ?? { id: estadoIdSelecionado, sigla: '', nome: '' }
    };

    if (this.isEditMode && this.municipioId) {
      this.municipioService.update(this.municipioId, { ...dados, id: this.municipioId }).subscribe({
        next: () => {
          this.snackBar.open('Município atualizado com sucesso!', 'Fechar', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['psi-snackbar']
          });
          this.router.navigate(['/municipios']);
        },
        error: (err) => {
          console.error(err);
          this.snackBar.open('Erro ao atualizar o município. Verifique os dados.', 'Fechar', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        }
      });
    } else {
      this.municipioService.create(dados).subscribe({
        next: () => {
          this.snackBar.open('Município cadastrado com sucesso!', 'Fechar', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['psi-snackbar']
          });
          this.router.navigate(['/municipios']);
        },
        error: (err) => {
          console.error(err);
          this.snackBar.open('Erro ao cadastrar o município. Verifique os dados informados.', 'Fechar', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        }
      });
    }
  }

  excluir(): void {
    if (!this.municipioId) return;

    const confirmacao = window.confirm('Deseja realmente excluir este município?');
    if (!confirmacao) return;

    this.municipioService.delete(this.municipioId).subscribe({
      next: () => {
        this.snackBar.open('Município excluído com sucesso!', 'Fechar', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['psi-snackbar']
        });
        this.router.navigate(['/municipios']);
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Erro ao excluir o município.', 'Fechar', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/municipios']);
  }
}
