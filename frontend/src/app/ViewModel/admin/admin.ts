import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { CheckToken } from '../../helpers/genarateHeader';
import { AdminService } from '../../services/admin.service';
import { User } from '../../models/User';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { UserService } from '../../services/user.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin implements OnInit {
  public token: String | null = null;
  public search: string = '';
  public usersStates: User[] = [];
  public OriginaleUsers : User[] = [];
  public filtredUsers : User[] = [];
  public isBrowser: boolean = false;

  public constructor(private router: Router, private adminService: AdminService, @Inject(PLATFORM_ID) platformId: Object) { 
    this.isBrowser = isPlatformBrowser(platformId)
  }

  ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }
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
        this.OriginaleUsers = res.users;
        this.usersStates = this.OriginaleUsers;
        this.filtredUsers = this.OriginaleUsers;
        console.log(this.filtredUsers);

      },
      error: (err) => {
        if (err.status == 401) {
          this.router.navigate(["login"])
        }
        // console.log(err);
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

  Filter() {
    console.log(this.search);
    
    if (this.search.length === 0) {
      console.log("hanni");
      
      this.filtredUsers = this.OriginaleUsers;
    }
    this.filtredUsers = this.usersStates.filter((u) => {
      return u.username.startsWith(this.search)
    })
    console.log(this.OriginaleUsers);
    
    console.log(this.filtredUsers);
    
  }
}
