import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout.component';
import { LoginComponent } from './components/login/login.component';
import { EstadoListComponent } from './components/estado/estado-list/estado-list.component';
import { EstadoFormComponent } from './components/estado/estado-form/estado-form.component';
import { MunicipioListComponent } from './components/municipio/municipio-list/municipio-list.component';
import { MunicipioFormComponent } from './components/municipio/municipio-form/municipio-form.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'PSI - Autenticação'
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'estados', pathMatch: 'full' },
      { path: 'estados', component: EstadoListComponent, title: 'PSI - Estados' },
      {
        path: 'estados/novo',
        component: EstadoFormComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMINISTRADOR', 'ATENDENTE'] },
        title: 'PSI - Novo Estado'
      },
      {
        path: 'estados/editar/:id',
        component: EstadoFormComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMINISTRADOR', 'ATENDENTE'] },
        title: 'PSI - Editar Estado'
      },
      { path: 'municipios', component: MunicipioListComponent, title: 'PSI - Municípios' },
      {
        path: 'municipios/novo',
        component: MunicipioFormComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMINISTRADOR', 'ATENDENTE'] },
        title: 'PSI - Novo Município'
      },
      {
        path: 'municipios/editar/:id',
        component: MunicipioFormComponent,
        canActivate: [authGuard],
        data: { roles: ['ADMINISTRADOR', 'ATENDENTE'] },
        title: 'PSI - Editar Município'
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
