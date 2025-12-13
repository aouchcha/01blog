import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { ToastService } from '../services/toast.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  const router = inject(Router);
  const location = inject(Location);

  return next(req).pipe(

    catchError(err => {
      console.log(err.error?.error);

      switch (err.status) {
        case 0:
          toast.showError(`No Connection — Please check your internet connection.`);
          break;

        case 400:
          toast.showError(`Bad Request — ${err.error?.error || 'Invalid request'}.`);
          break;

        case 401:
          toast.showError("Unauthorized.");
          localStorage.removeItem("JWT");
          router.navigate(['/login']);          
          break;

        case 403:
          toast.showError("Forbidden — You don't have permission to access this resource.");
          localStorage.removeItem("JWT");
          router.navigate(['/login']);
          break;

        case 404:
          toast.showError(`Not Found — ${err.error?.error || 'Resource not found'}.`);
          console.log({ "message": "Error 404" });
          location.back();
          break;

        case 500:
          console.log({ "message": "Error 500" });
          toast.showError(`Internal Server Error — ${err.error?.error || 'Something went wrong'}.`);
          break;

        default:
        toast.showError("Unexpected error occurred.");
      }

      return throwError(() => err);
    }),

    tap({
      next: (event) => {
        if (event instanceof HttpResponse) {
          const body = event.body as any;
          // console.log({body});
          
          if (!body?.message) return;
          switch (event.status) {
            case 200:
                toast.showSuccess(body?.message);
              break;

            case 201:
              toast.showSuccess(body?.message || "Resource created successfully!");
              break;

            case 204:
              toast.showSuccess(body?.message || "Resource deleted successfully!");
              break;

            default:
              toast.showSuccess(body?.message);
          }
        }
      }
    })
  );
};