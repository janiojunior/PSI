import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { NomePerfil } from '../models/auth.model';

/**
 * Guard funcional de rotas (`CanActivateFn`) responsável por:
 * 1. Garantir que o usuário possua uma sessão autenticada e com token JWT não expirado.
 * 2. Validar a autorização baseada em perfis (`roles`), caso a rota exija perfis
 *    específicos em `route.data['roles']` (ex.: `ADMINISTRADOR`, `ATENDENTE`).
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  // 1. Verifica se o usuário está autenticado; caso contrário, redireciona para /login
  // preservando a URL original em `returnUrl` para redirecionamento pós-login
  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], {
      queryParams: state.url && state.url !== '/' ? { returnUrl: state.url } : undefined
    });
  }

  // 2. Verifica se a rota exige perfis de acesso específicos (RBAC)
  const rolesExigidas = route.data?.['roles'] as NomePerfil[] | undefined;
  if (rolesExigidas && rolesExigidas.length > 0 && !authService.hasAnyRole(rolesExigidas)) {
    // Exibe feedback visual informando a falta de permissão e redireciona para uma rota segura
    snackBar.open('Seu perfil não possui permissão para acessar esta funcionalidade.', 'Fechar', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['psi-snackbar']
    });
    return router.createUrlTree(['/estados']);
  }

  // Permite a ativação da rota quando autenticado e autorizado
  return true;
};
