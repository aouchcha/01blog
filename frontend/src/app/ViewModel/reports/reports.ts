import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CheckToken } from '../../helpers/genarateHeader';
import { Router } from '@angular/router';
import { Confirmation } from '../confirmation/confirmation';
import { UserService } from '../../services/user.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-reports',
  imports: [
    CommonModule,
    Confirmation,
    MatIconModule
  ],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit {
  public token: String | null = null;
  public reports: any = [];
  public username: string | null = null;
  public report_id: number | null = null;
  public lastReport: any = null;
  public isLoading: boolean = false;
  public HasMoreReports: boolean = true;
  public total: number = 0;
  public showConfirmation: boolean = false;
  public confirmationTitle: string = 'Delete Report?';
  public confirmationMessage: string = 'Are you sure you want to delete this Report? This action cannot be undone.';
  public confirmationAction: string = 'Delete';


  public constructor(private adminService: AdminService, private router: Router, private userService: UserService) { }

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

  public handleScrollLogic(event: any): void {
    const element = event.target;

    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 50;
    if (atBottom && !this.isLoading && this.HasMoreReports) {
      this.LoadReports();
    }
  }

  public LoadReports() {
    this.setToken();
    if (this.isLoading) return;

    this.adminService.loadReports(this.token, this.lastReport).subscribe({
      next: (res) => {
        if (res.reports && res.reports.length > 0) {
          this.reports = [...this.reports, ...res.reports];
          this.lastReport = this.reports[this.reports.length - 1];
          this.total = res.reportsCount;
          this.isLoading = false;
        } else {
          this.HasMoreReports = false;
        }
      },
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

  HandleAction(value: boolean) {
    if (!value) {
      this.CancelAction()
    } else {
      if (this.confirmationAction === "Delete") {
        this.DeleteReport()
      } else if (this.confirmationAction === "Ban") {
        this.BanUser()
        this.DeleteReport()
      }
      this.CancelAction()
    }
  }

  CheckBanUser(username: string, report_id: number) {
    this.showConfirmation = true;
    this.username = username;
    this.report_id = report_id;

    this.confirmationAction = "Ban";
    this.confirmationMessage = "Are you sure you want to Ban this user? This action cannot be undone."
    this.confirmationTitle = `Ban User: ${username} ?`;
  }

  CancelAction() {
    this.showConfirmation = false;
    this.confirmationTitle = 'Delete Report?';
    this.confirmationMessage = 'Are you sure you want to delete this Report? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.username = null;
    this.showDetailsModal = false;
  }

  ConfirmAction(report_id: number) {
    this.showConfirmation = true;
    this.report_id = report_id;
    this.username = null;
  }

  DeleteReport() {
    this.adminService.RemoveReport(this.token, this.report_id).subscribe({
      next: () => {
        let index = this.reports.findIndex((r: any) => r.id == this.report_id)
        if (index === -1) return;
        this.reports.splice(index, 1)
        this.lastReport = this.reports[this.reports.length - 1];
        this.total -= 1;
        this.report_id = null;
      },
    })

  }

  BanUser() {
    this.userService.BanUserr(this.username, this.token).subscribe({

    })
  }

  GoToPost(postId: number) {
    this.router.navigate([`post/${postId}`]);
  }
}
