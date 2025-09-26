import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthModel } from '../../services/auth.service';
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
  error: String = '';
  // sss: ChangeDetectorRef;

  constructor(private router: Router, private authModel: AuthModel, private stateup: ChangeDetectorRef) { };

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
      "username": this.username,
      "password": this.password,
      "email": this.email
    }

    this.authModel.Register(user).subscribe({
      next: (response) => {
        console.log('Registration successful:', response);
        // Redirect to login on success
        this.router.navigate(['login']);
      },
      error: (error) => {
        this.error = error.error;
        this.stateup.detectChanges();
        setTimeout(() => {
          this.error = '';
          this.stateup.detectChanges();
        }, 5000);
      }
    })
  }
}
