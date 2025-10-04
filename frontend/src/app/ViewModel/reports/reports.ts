import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CheckToken } from '../../helpers/genarateHeader';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reports',
  imports: [
    CommonModule
  ],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit {
  public token: String | null = null;
  public reports: any = [];

  public constructor(private adminService: AdminService, private router: Router) { }
  ngOnInit(): void {
    this.setToken();
    this.LoadReports();
  }

   public setToken() {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    this.token = localStorage.getItem("JWT");
  }

  public LoadReports() {
    this.setToken();
    this.adminService.loadReports(this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.reports = res.reports;
      },
      error: (err) => {
        console.log(err);
      } 
    })
  }

}
