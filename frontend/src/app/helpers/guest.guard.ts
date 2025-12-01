// src/app/helpers/guest.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

export const guestGuard: CanActivateFn = (route, state) => {
    console.log("Guest Guard");
    
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);

    // Check if running in browser
    if (!isPlatformBrowser(platformId)) {
        return true; // Allow access during SSR (will check again in browser)
    }

    const token = localStorage.getItem('token');
    console.log({token});
    
    
    if (token) {
        // User is already logged in, redirect to home
        router.navigate(['/']);
        return false;
    }
    
    return true;
};