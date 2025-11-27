import { HttpInterceptorFn, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
// import { SnakService } from '../services/snack';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
//   const notif = inject(SnakService);
  const router = inject(Router);

  return next(req).pipe(
    catchError(err => {
      switch (err.status) {
        case 0:
            console.log({"message": "No Connection"});
            break;
             
        case 400:
        //   notif.showError("Bad Request — check your data.");
        console.log({"message":"Error 400"});
        
          break;
        case 401:
        //   notif.showError("Unauthorized — login required.");
          localStorage.removeItem("JWT");
            console.log({"message":"Error 401"});

        //   router.navigate(['/login']);
          break;
        case 403:
        //   notif.showError("Forbidden — you are banned.");
        console.log({"message":"Error 403"});

          break;
        case 404:
        //   notif.showError("Resource not found.");
        console.log({"message":"Error 404"});

          break;
        case 500:
        console.log({"message":"Error 500"});
        //   notif.showError("Server error — try again later.");
          break;
        default:
        console.log({"message":"Wa L9lawi"});

        //   notif.showError("Unexpected error occurred.");
      }
      return throwError(() => err);
    })
  );
};