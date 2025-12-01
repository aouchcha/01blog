// src/app/helpers/guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);

    // Check if running in browser
    if (!isPlatformBrowser(platformId)) {
        return false; // Block access during SSR
    }

    const token = localStorage.getItem('token');

    if (!token) {
        router.navigate(['/login'], { 
            queryParams: { returnUrl: state.url } 
        });
        return false;
    }

    // Quick JWT validation
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expirationTime = payload.exp * 1000;
        
        if (Date.now() >= expirationTime) {
            localStorage.removeItem('token');
            router.navigate(['/login'], { 
                queryParams: { returnUrl: state.url } 
            });
            return false;
        }
        
        return true;
    } catch (error) {
        localStorage.removeItem('token');
        router.navigate(['/login'], { 
            queryParams: { returnUrl: state.url } 
        });
        return false;
    }
};