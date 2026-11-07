import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor funcional HTTP responsável por:
 * 1. Anexar automaticamente o cabeçalho `Authorization: Bearer <token>` em todas as
 *    requisições destinadas à API quando houver uma sessão autenticada ativa.
 * 2. Interceptar respostas de erro `401 Unauthorized` (token expirado ou inválido),
 *    encerrando a sessão local e redirecionando o usuário para a tela de login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Injeta o serviço de autenticação para consultar o Signal do token JWT atual
  const authService = inject(AuthService);
  const token = authService.token();

  // Evita anexar o token ou disparar logout automático na própria rota de login
  const isAuthLoginEndpoint = req.url.endsWith('/auth/login');

  // Clona a requisição original adicionando o header Authorization caso exista token válido
  const reqAutenticada =
    token && !isAuthLoginEndpoint
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : req;

  // Encaminha a requisição e monitora falhas de autenticação retornadas pelo Backend
  return next(reqAutenticada).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se o Backend rejeitar o token com 401, limpa a sessão e redireciona para /login
      if (error.status === 401 && !isAuthLoginEndpoint) {
        authService.logout(true);
      }
      // Propaga o erro para que o componente/serviço chamador também possa tratá-lo
      return throwError(() => error);
    })
  );
};
