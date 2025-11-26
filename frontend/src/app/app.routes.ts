import { Routes } from '@angular/router';
import { Login } from './ViewModel/login/login';
import { Register } from './ViewModel/register/register';
import { Home } from './ViewModel/home/home';
import { SinglePost } from './ViewModel/single-post/single-post';
import { Profile } from './ViewModel/profile/profile';
import { Admin } from './ViewModel/admin/admin';
import { Reports } from './ViewModel/reports/reports';

export const routes: Routes = [
    { path: "register", component: Register },
    { path: "login", component: Login },
    { path: "", component: Home },
    { path: 'admin', component: Admin },
    { path: 'post/:id', component: SinglePost },
    { path: 'user/:username', component: Profile, runGuardsAndResolvers: 'always'},
    { path: 'reports', component: Reports }
];
