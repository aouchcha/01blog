import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { CheckToken } from '../../helpers/genarateHeader';
import { AdminService } from '../../services/admin.service';
import { User } from '../../models/User';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { UserService } from '../../services/user.service';
import { FormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIcon
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin implements OnInit {
  public token: String | null = null;
  public search: string = '';
  public admin: User = new User();
  public usersStates: User[] = [];
  public OriginaleUsers: User[] = [];
  public filtredUsers: any = [];
  public isBrowser: boolean = false;
  public report_count: number = 0;
  public isLoading: boolean = false;
  public lastUserId: number | null = null;
  public HasMoreUsers: boolean = true;


  public constructor(private router: Router, private adminService: AdminService, private userService: UserService, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId)
  }

  ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }

    this.token = CheckToken();
    this.getAdminInfo()
    this.getDashboard()
  }

  public getAdminInfo() {
    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        this.admin = res.me;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public handleScrollLogic(event: any): void {
    const element = event.target;

    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 50;
    if (atBottom && !this.isLoading && this.HasMoreUsers) {
      console.log({ "mwssage": "wsset lekher" });

      this.getDashboard();
    }
  }

  public getDashboard() {
    if (this.isLoading) return;

    this.adminService.getDashBoard(this.token, this.lastUserId).subscribe({
      next: (res) => {
        console.log({ res });

        if (res.users && res.users.length > 0) {
          this.OriginaleUsers = [...this.OriginaleUsers, ...res.users];
          this.lastUserId = this.OriginaleUsers[this.OriginaleUsers.length - 1].id;
          this.isLoading = false;

        } else {
          this.HasMoreUsers = false;
        }

        this.usersStates = this.OriginaleUsers;
        this.filtredUsers = this.OriginaleUsers;
        this.report_count = res.reportsCount;

      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public ToProfile(username: String) {
    console.log({ username });

    this.router.navigate([`user/${username}`])
  }

  public Logout() {
    localStorage.removeItem("JWT");
    this.token = null;
    this.router.navigate(["login"])
  }

  public ShowReports() {

    this.router.navigate(["reports"])
  }

  Filter() {
    console.log(this.search);

    if (this.search.length === 0) {
      this.filtredUsers = this.OriginaleUsers;
    }
    this.filtredUsers = this.usersStates.filter((u) => {
      return u.username?.startsWith(this.search)
    })

  }

  public ToDashboard() {
    this.router.navigate(["admin"])
  }
}
