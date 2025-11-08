import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
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
    public isBrowser: boolean = false;


  public constructor(private adminService: AdminService, private router: Router, @Inject(PLATFORM_ID) platformId: Object) { 
    this.isBrowser = isPlatformBrowser(platformId)
  }
  ngOnInit(): void {
     if (!this.isBrowser) {
      return
    }
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

  public ToDashBoard() {
    this.router.navigate(["admin"])
  }

  selectedReport: any = null;
  showDetailsModal: boolean = false;

  ViewDetails(report: any) {
    this.selectedReport = report;
    this.showDetailsModal = true;
  }

  CloseModal() {
    this.showDetailsModal = false;
  }

  DeleteReport(id: number) {
    console.log(id);
  }

  ResolveReport(id: number) {
    console.log(id);
  }
}
