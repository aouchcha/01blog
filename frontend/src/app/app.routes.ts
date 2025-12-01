// src/app/routes.ts
import { Routes } from '@angular/router';
import { Login } from './ViewModel/login/login';
import { Register } from './ViewModel/register/register';
import { Home } from './ViewModel/home/home';
import { SinglePost } from './ViewModel/single-post/single-post';
import { Profile } from './ViewModel/profile/profile';
import { Admin } from './ViewModel/admin/admin';
import { Reports } from './ViewModel/reports/reports';

import { authGuard } from './helpers/guard';
import { guestGuard } from './helpers/guest.guard';

export const routes: Routes = [
    { 
        path: "register", 
        component: Register,
        canActivate: [guestGuard]
    },
    { 
        path: "login", 
        component: Login,
        canActivate: [guestGuard]
    },
    { 
        path: "", 
        component: Home,
        // canActivate: [authGuard]
    },
    { 
        path: 'admin', 
        component: Admin,
        canActivate: [authGuard] 
    },
    { 
        path: 'post/:id', 
        component: SinglePost,
        canActivate: [authGuard]
    },
    { 
        path: 'user/:username', 
        component: Profile,
        canActivate: [authGuard],
        runGuardsAndResolvers: 'always'
    },
    { 
        path: 'reports', 
        component: Reports,
        canActivate: [authGuard] 
    },
];