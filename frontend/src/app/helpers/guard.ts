// src/app/helpers/guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';


export const authGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);

    const token = localStorage.getItem('JWT');

    if (!token) {
        router.navigate(['/login'], {
            queryParams: { returnUrl: state.url }
        });
        return false;
    }
    return true;

};