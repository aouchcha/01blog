import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CheckToken } from '../../helpers/genarateHeader';
import { Router } from '@angular/router';
import { Confirmation } from '../confirmation/confirmation';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-reports',
  imports: [
    CommonModule,
    Confirmation
  ],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit {
  public token: String | null = null;
  public reports: any = [];
  public isBrowser: boolean = false;
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


  public constructor(private adminService: AdminService, private router: Router, @Inject(PLATFORM_ID) platformId: Object, private userService: UserService) {
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

  public handleScrollLogic(event: any): void {
    const element = event.target;

    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 50;
    if (atBottom && !this.isLoading && this.HasMoreReports) {
      console.log({ "mwssage": "wsset lekher" });

      this.LoadReports();
    }
  }

  public LoadReports() {
    this.setToken();
    if (this.isLoading) return;

    this.adminService.loadReports(this.token, this.lastReport).subscribe({
      next: (res) => {
        console.log({res});
        if (res.reports && res.reports.length > 0) {
          this.reports = [...this.reports, ...res.reports];
          this.lastReport = this.reports[this.reports.length - 1];
          this.total = res.reportsCount;
          this.isLoading = false;
        }else {
          this.HasMoreReports = false;
        }
        // this.reports = res.reports;
        console.log({"length :": this.reports.length});
        
      },
      error: (err) => {
        console.log(err);
        if (err.status == 401) {
          this.router.navigate(["login"])
        }
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

  HandleAction(value: boolean) {
    if (!value) {
      console.log("Cancel");

      this.CancelAction()
    } else {
      if (this.confirmationAction === "Delete") {
        this.DeleteReport()
        // this.CancelAction();
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
    console.log(report_id);
    console.log(username);


    this.confirmationAction = "Ban";
    this.confirmationMessage = "Are you sure you want to Ban this user? This action cannot be undone."
    this.confirmationTitle = `Ban User: ${username} ?`;
  }

  CancelAction() {
    this.showConfirmation = false;
    this.confirmationTitle = 'Delete Report?';
    this.confirmationMessage = 'Are you sure you want to delete this Report? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    // this.showConfirmation = false;
    this.username = null;
    this.showDetailsModal = false;
    // this.report_id = null;
  }

  ConfirmAction(report_id: number) {
    this.showConfirmation = true;
    this.report_id = report_id;
    this.username = null;
  }

  DeleteReport() {
    console.log({ "RRRRRRRRR": this.report_id });

    this.adminService.RemoveReport(this.token, this.report_id).subscribe({
      next: (res) => {
        console.log({"reports :":this.reports});
        let index = this.reports.findIndex((r: any) => r.id == this.report_id)
        console.log({index});
        
        this.reports.splice(index, 1)
        this.lastReport = this.reports[this.reports.length - 1];
        this.total -= 1;
        this.report_id = null;
        // this.showConfirmation = false;
      },
      error: (err) => {
        console.log(err);
      }
    })

  }

  BanUser() {
    console.log("Ban User");
    this.userService.BanUserr(this.username, this.token).subscribe({
      next: (res) => {
        // this.user.isbaned = !this.user.isbaned;
        console.log(res);
        // this.showConfirmation = false;
        // this.DeleteReport();
      },
      error: (err) => {
        console.log(err);
      }
    })
  }
}
