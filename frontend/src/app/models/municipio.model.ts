import { Estado } from './estado.model';

export interface Municipio {
  id?: number;
  nome: string;
  estado: Estado;
}
