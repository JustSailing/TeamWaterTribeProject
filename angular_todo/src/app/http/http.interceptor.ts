import { HttpInterceptorFn } from '@angular/common/http';

export const contentTypeJsonInterceptor: HttpInterceptorFn = (req, next) => {
  const hasContentType = req.headers.has('Content-Type');
  const isFormData = req.body instanceof FormData;

  if (!hasContentType && !isFormData) {
    req = req.clone({
      setHeaders: {
        'Content-Type': 'application/json'
      }
    });
  }

  return next(req);
};