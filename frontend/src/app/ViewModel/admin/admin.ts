import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CheckToken } from '../../helpers/genarateHeader';
import { AdminService } from '../../services/admin.service';
import { User } from '../../models/User';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin implements OnInit {
  public token: String | null = null;
  public usersStates: User[] = [];

  public constructor(private router: Router, private adminService: AdminService) { }

  ngOnInit(): void {
    if (CheckToken() === null) {
      console.log("token Not FOund");
      this.router.navigate(["login"])
      return
    }
    this.token = CheckToken();
    this.getDashboard()

  }

  public getDashboard() {
    this.adminService.getDashBoard(this.token).subscribe({
      next: (res) => {
        this.usersStates = res.users;
        console.log(this.usersStates);

      },
      error: (err) => {
        if (err.status == 401) {
          this.router.navigate(["login"])
        }
        console.log(err);
      }
    })
  }

  public ToProfile(username: String) {
    console.log({ username });

    this.router.navigate([`user/${username}`])
  }

  public Home() {
    this.router.navigate([''])
  }

  public Logout() {
    localStorage.removeItem("JWT");
    this.token = null;
    this.router.navigate(["login"])
  }

  public ShowReports() {
    console.log({"toreport":""});
    
    this.router.navigate(["reports"])
  }
}
