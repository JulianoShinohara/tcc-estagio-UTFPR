import { Routes } from '@angular/router';
import { EstudanteComponent } from './componentes/estudante/estudante.component';
import { OrientadorComponent } from './componentes/orientador/orientador.component';
import { PraeComponent } from './componentes/prae/prae.component';

export const routes: Routes = [
  { path: 'estudante', component: EstudanteComponent },
  { path: 'orientador', component: OrientadorComponent },
  { path: 'prae', component: PraeComponent },
  { path: '', redirectTo: '/estudante', pathMatch: 'full' },
];
