import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login').then((m) => m.Login) },
  {
    path: 'inicio',
    canActivate: [authGuard],
    loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio),
  },
  {
    path: 'productos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/productos/productos').then((m) => m.Productos),
  },
  {
    path: 'clientes',
    canActivate: [authGuard],
    loadComponent: () => import('./features/clientes/clientes').then((m) => m.Clientes),
  },
  {
    path: 'ventas',
    canActivate: [authGuard],
    loadComponent: () => import('./features/ventas/ventas').then((m) => m.Ventas),
  },
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },
  { path: '**', redirectTo: 'inicio' },
];
