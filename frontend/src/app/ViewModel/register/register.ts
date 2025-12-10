import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { authService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-register',
  imports: [
    CommonModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  username: string = '';
  password: string = '';
  email: string = '';
  error: String = '';

  constructor(private router: Router, private authService: authService, private stateup: ChangeDetectorRef) { };

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
    this.router.navigate(["login"]);
  }

  public SignUp() {
    const user = {
      "username": this.username,
      "password": this.password,
      "email": this.email
    }

    this.authService.Register(user).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
    })
  }
}
