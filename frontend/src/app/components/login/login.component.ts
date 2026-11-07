import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  readonly authService = inject(AuthService);

  readonly mostrarSenha = signal<boolean>(false);

  readonly form: FormGroup = this.fb.group({
    login: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    senha: ['', [Validators.required]]
  });

  constructor() {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/estados']);
    }
  }

  alternarVisibilidadeSenha(): void {
    this.mostrarSenha.update((v) => !v);
  }

  preencherCredenciaisAdminPadrao(): void {
    this.form.patchValue({
      login: 'admin@psi.com.br',
      senha: 'Admin@Psi2026!Segura'
    });
    this.form.markAsDirty();
  }

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const credenciais = {
      login: (this.form.value.login as string).trim().toLowerCase(),
      senha: this.form.value.senha as string
    };

    this.authService.login(credenciais).subscribe({
      next: (res) => {
        this.snackBar.open(`Bem-vindo(a), ${res.usuario.nome}!`, 'Fechar', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['psi-snackbar']
        });
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/estados';
        this.router.navigateByUrl(returnUrl);
      }
    });
  }
}
