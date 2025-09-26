import { Routes } from '@angular/router';
import { Login } from './ViewModel/login/login';
import { Register } from './ViewModel/register/register';
import { Home } from './ViewModel/home/home';
import { SinglePost } from './ViewModel/single-post/single-post';
import { Profile } from './ViewModel/profile/profile';
import { Admin } from './ViewModel/admin/admin';

export const routes: Routes = [
    { path: "login", component: Login },
    { path: "register", component: Register },
    { path: "", component: Home },
    { path: 'post/:id', component: SinglePost },
    { path: 'admin', component: Admin },
    { path: ':username', component: Profile },
];
