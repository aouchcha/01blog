import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component } from '@angular/core';
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
  error : string = '';
  res : any = {"token": ''};
  constructor(private router : Router, private http: HttpClient, private stateup: ChangeDetectorRef){};
 
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
    console.log("wesh wesh");
    
    const body = {
      "username" : this.username,
      "password" : this.password
    }

    this.http.post('http://localhost:8080/api/login', body).subscribe({
      next: (res) => {
        console.log('log in successful !', typeof res, res);
        this.res = res;
        localStorage.setItem("JWT", this.res.token);
        this.router.navigate([""]);
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
