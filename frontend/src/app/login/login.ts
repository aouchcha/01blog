import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username : string = '';
  password : string = '';
  
  constructor(public router : Router){};
 
  public setUsername(username: string): void {
    this.username = username;
  }

  public setPassword(password: string): void {
    this.password = password;
  }

  public ToRegister() {
    this.router.navigate(["register"]);
  }
}
