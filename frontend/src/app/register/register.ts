import { Component } from '@angular/core';
import { Router } from '@angular/router';
@Component({
  selector: 'app-register',
  imports: [],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  username: string = '';
  password: string = '';
  email: string = '';
  constructor(public router: Router) { };

  public setUsername(username: string): void {
    // const target = event.target as HTMLInputElement;
    this.username = username;
  }

  public setPassword(password: string): void {
    // const target = event.target as HTMLInputElement;
    this.password = password;
  }

  public setEmail(email: string): void {
    // const target = event.target as HTMLInputElement;
    this.email = email;
  }

  public ToLogin() {
    console.log("hanni");

    this.router.navigate(["login"]);
  }
}
