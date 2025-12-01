import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ToastService } from '../services/toast.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  const router = inject(Router);

  return next(req).pipe(

    catchError(err => {
      // const body = err;
      console.log(err.error.error); // Access the message field

      switch (err.status) {
        case 0:
          console.log({ "message": "No Connection" });
          router.navigate(["/login"])
          break;

        case 400:
          toast.showError(`Bad Request — ${err.error.error}.`);
          console.log({ "message": "Error 400" });
          router.navigate([""])


          break;
        case 401:
          toast.showError("Unauthorized.");
          localStorage.removeItem("JWT");
          console.log({ "message": "Error 401" });

          router.navigate(['/login']);
          break;
        case 403:
          toast.showError("Forbidden");
          console.log({ "message": "Error 403" });
          router.navigate(['/login']);
          break;
        case 404:
          toast.showError(`Not Found — ${err.error.error}.`);
          console.log({ "message": "Error 404" });
          router.navigate([""])


          break;
        case 500:
          console.log({ "message": "Error 500" });
          toast.showError(`Internal Server Error — ${err.error.error}.`);
          router.navigate([""])

          break;
        default:
          // console.log({ "message": "Wa L9lawi" });

          toast.showError("Unexpected error occurred.");
      }
      return throwError(() => err);
    }),
    tap({
      next: (event) => {
        if (event instanceof HttpResponse) {
          const body = event.body as any;
          console.log({ "body": event.body });
          console.log({ "message": body?.message }); // Access the message field

          switch (event.status) {
            case 200:
              console.log({ "message": "Success 200" });
              if (body?.message) {
                toast.showSuccess(body.message); // Use the message from response
              }
              break;

            case 201:
              console.log({ "message": "Created 201" });
              toast.showSuccess(body?.message || "Resource created successfully!");
              break;

            case 204:
              console.log({ "message": "No Content 204" });
              // Note: 204 typically has no body
              break;

            default:
              console.log({ "message": `Success ${event.status}` });
              if (body?.message) {
                console.log({ "server_message": body.message });
              }
          }
        }
      }
    })
  );
};