import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout.component';
import { EstadoListComponent } from './components/estado/estado-list/estado-list.component';
import { EstadoFormComponent } from './components/estado/estado-form/estado-form.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'estados', pathMatch: 'full' },
      { path: 'estados', component: EstadoListComponent, title: 'PSI - Estados' },
      { path: 'estados/novo', component: EstadoFormComponent, title: 'PSI - Novo Estado' },
      { path: 'estados/editar/:id', component: EstadoFormComponent, title: 'PSI - Editar Estado' }
    ]
  },
  { path: '**', redirectTo: '' }
];
