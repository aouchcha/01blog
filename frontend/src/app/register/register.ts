import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

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

  constructor(public router: Router, public http: HttpClient) { };

  public setUsername(username: string): void {
    this.username = username;
  }

  public setPassword(password: string): void {
    this.password = password;
  }

  public setEmail(email: string): void {
    this.email = email;
  }

  public ToLogin() {
    console.log("hanni");

    this.router.navigate(["login"]);
  }

  public SignUp() {
    const user = {
      "username" : this.username,
      "password" : this.password,
      "email" : this.email
    }

    // console.log("|||||||||||||||||||||||||||||||||||",user);
    

    this.http.post('http://localhost:8080/api/register', user).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        // Redirect to login on success
        // this.router.navigate(['login']);
      },
      error: (error) => {
        console.error('Registration failed:', error);
        alert('Registration failed. Please try again.');
      }
    })
  }
}
