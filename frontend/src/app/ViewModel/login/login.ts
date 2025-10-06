import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { authService } from '../../services/auth.service';
import { NotificationsService } from '../../services/notification.service';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username : string = '';
  password : string = '';
  error : string = '';
  constructor(private router : Router, private stateup: ChangeDetectorRef, private authService: authService, private notifService: NotificationsService){};
 
  public setUsername(username: string): void {
    this.username = username;
  }

  public setPassword(password: string): void {
    this.password = password;
  }

  public ToRegister() {
    this.router.navigate(["register"]);
  }

  public Submit() {
    // console.log("wesh wesh");
    
    const body = {
      "username" : this.username,
      "password" : this.password
    }

    this.authService.Login(body).subscribe({
      next: (res) => {
        console.log('log in successful !', res.user.role, res);
        // this.res = res;
        localStorage.setItem("JWT", res.token);
        if (res.user.role == "User") {
          this.router.navigate([""]);
        }else {
          this.router.navigate(["admin"])
        }
      },
      error: (error) => {
        console.log(error);
        
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
