import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const guestGuard: CanActivateFn = () => {
    
    const router = inject(Router);

    const token = localStorage.getItem('JWT');    
    
    if (token) {
        router.navigate(['/']);
        return false;
    }
    
    return true;
};