import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Inject AuthService to access the stored JWT token
  const authService = inject(AuthService);

  const token = authService.token();

  if (token) {
    const clonedReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });

    return next(clonedReq);
  }

  return next(req);
};