import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Home } from './home/home';
import { SinglePost } from './single-post/single-post';
import { Profile } from './profile/profile';

export const routes: Routes = [
    { path: "login", component: Login },
    { path: "register", component: Register },
    { path: "", component: Home },
    { path: 'post/:id', component: SinglePost },
    { path: ':username', component: Profile }
];
