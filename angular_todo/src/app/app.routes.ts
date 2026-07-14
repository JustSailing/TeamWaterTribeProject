import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Home } from './home/home';
import { Todo } from './pages/todo/todo';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'register', component: Register },
  {
    path: 'home',
    component: Home,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'todos', pathMatch: 'full' },
      { path: 'todos', component: Todo },
    ],
  },
];