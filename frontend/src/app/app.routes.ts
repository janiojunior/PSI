import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout.component';
import { EstadoListComponent } from './components/estado/estado-list/estado-list.component';
import { EstadoFormComponent } from './components/estado/estado-form/estado-form.component';
import { MunicipioListComponent } from './components/municipio/municipio-list/municipio-list.component';
import { MunicipioFormComponent } from './components/municipio/municipio-form/municipio-form.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'estados', pathMatch: 'full' },
      { path: 'estados', component: EstadoListComponent, title: 'PSI - Estados' },
      { path: 'estados/novo', component: EstadoFormComponent, title: 'PSI - Novo Estado' },
      { path: 'estados/editar/:id', component: EstadoFormComponent, title: 'PSI - Editar Estado' },
      { path: 'municipios', component: MunicipioListComponent, title: 'PSI - Municípios' },
      { path: 'municipios/novo', component: MunicipioFormComponent, title: 'PSI - Novo Município' },
      { path: 'municipios/editar/:id', component: MunicipioFormComponent, title: 'PSI - Editar Município' }
    ]
  },
  { path: '**', redirectTo: '' }
];
