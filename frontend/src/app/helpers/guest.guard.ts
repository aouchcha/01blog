// src/app/helpers/guest.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const guestGuard: CanActivateFn = (route, state) => {
    console.log("Guest Guard");
    
    const router = inject(Router);

    const token = localStorage.getItem('JWT');
    console.log({token});
    
    
    if (token) {
        router.navigate(['/']);
        return false;
    }
    
    return true;
};