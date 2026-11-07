import { Municipio } from './municipio.model';

export type NomePerfil = 'ADMINISTRADOR' | 'ATENDENTE' | 'PACIENTE' | 'PSICOLOGO';

export interface Perfil {
  id: number;
  nome: string;
}

export interface UsuarioAutenticado {
  id: number;
  login: string;
  cpf: string;
  nome: string;
  email: string;
  genero?: string | null;
  endereco?: string | null;
  municipio?: Municipio | null;
  ativo: boolean;
  perfis: Perfil[];
}

export interface AuthRequest {
  login: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  usuario: UsuarioAutenticado;
}
