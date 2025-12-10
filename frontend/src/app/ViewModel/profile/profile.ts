import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CheckToken } from '../../helpers/genarateHeader';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/User';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { PostsService } from '../../services/posts.service';
import { Post } from '../../models/Post';
import { Confirmation } from '../confirmation/confirmation';
import { NotificationsService } from '../../services/notification.service';
import { Subject, takeUntil } from 'rxjs';
import { ToastService } from '../../services/toast.service';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    FormsModule,
    Confirmation
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  public username: String = '';
  public token: String | null = null;
  public user: User = new User();
  public me: User = new User();
  public posts: any = [];
  public followers: number = 0;
  public followings: number = 0;
  public DoYouWantReport: boolean = false;
  public DoYouWantReportPost: boolean = false;
  public description: string = "";
  public showPopup: boolean = false;
  public ShowNotifs: boolean = false;
  public showConfirmation: boolean = false;
  public confirmationTitle: string = 'Delete Post?';
  public confirmationMessage: string = 'Are you sure you want to delete this post? This action cannot be undone.';
  public confirmationAction: string | undefined = 'Delete';
  public post_id: number | null = null;
  private destroy$ = new Subject<void>();
  private isLoading: boolean = false;
  private HasMorePosts: boolean = true;
  private lastPost: Post | null = null;
  public postscount: number = 0;

  constructor(private router: Router, private route: ActivatedRoute, private notifService: NotificationsService, private userService: UserService, private postService: PostsService, private toast: ToastService) {
  }

  public ngOnInit(): void {
    this.token = CheckToken();
    this.username = String(this.route.snapshot.paramMap.get('username'));



    this.getMe()
    this.LoadProfile()
    if (this.me.role !== 'ADMIN') {
      this.notifService.reactionsObservable
        .pipe(takeUntil(this.destroy$))
        .subscribe(react => {  
          let index = this.posts.findIndex((p: Post) => p.id === react.post.id);
          if (index === -1) return;
          this.posts[index].likeCount = react.post.likeCount;
        });
    }

  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public getMe() {
    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        this.me = res.me;
        if (this.me.role !== "Admin") {
          this.notifService.connect(this.me.id, this.token)
        }

      },
    })
  }


  public LoadProfile() {
    if (this.isLoading) return;
    this.userService.getProfile(this.username, this.token, this.lastPost).subscribe({
      next: (res) => {
        this.user = res.user;
        this.postscount = res.count;
        if (res.posts && res.posts.length > 0) {
          this.posts = [...this.posts, ...res.posts];
          this.lastPost = this.posts[this.posts.length - 1];
          this.followers = res.followers
          this.followings = res.followings
          this.isLoading = false
        } else {
          this.HasMorePosts = false;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    })
  }

  public handleScrollLogic(event: any): void {
    const element = event.target;

    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 100;

    if (atBottom && !this.isLoading && this.HasMorePosts) {
      this.LoadProfile();
    }
  }

  public Home() {
    this.router.navigate([""])
  }

  public ToDashBoard() {
    this.router.navigate(["admin"])
  }

  public Follow() {
    this.userService.Follow(this.user.id, this.token).subscribe({
      next: () => {
        this.LoadProfile()
      },
    })
  }

  public OpenReportSection() {
    this.DoYouWantReport = true;
    this.confirmationAction = "Report";
    this.confirmationMessage = "Are you sure you want to report this user? This action cannot be undone."
    this.confirmationTitle = `Report User: ${this.user.username} ?`;
    this.type = 'user';
  }

  public CheckBeforeRemoving() {
    this.showConfirmation = true;
    this.confirmationAction = "Remove";
    this.confirmationMessage = "Are you sure you want to remove this user? This action cannot be undone."
    this.confirmationTitle = `Remove User: ${this.user.username} ?`;
    this.type = 'user';
  }

  public CheckBeforeBan() {
    this.showConfirmation = true;
    this.confirmationAction = "Ban";
    this.confirmationMessage = "Are you sure you want to Ban this user? This action cannot be undone."
    this.confirmationTitle = `Ban User: ${this.user.username} ?`;
    this.type = 'user';
  }

  public CheckBeforeUnBan() {
    this.showConfirmation = true;
    this.confirmationAction = "UnBan";
    this.confirmationMessage = "Are you sure you want to UnBan this user? This action cannot be undone."
    this.confirmationTitle = `Ban User: ${this.user.username} ?`;
    this.type = 'user';
  }

  CheckBeforeHide(post_id: number) {
    this.confirmationTitle = 'Hide Post?';
    this.confirmationMessage = 'Are you sure you want to hide this post?';
    this.confirmationAction = 'Hide';
    this.showConfirmation = true;
    this.post_id = post_id;
    this.type = 'post';
  }

  CheckBeforeUnHide(post_id: number) {
    this.confirmationTitle = 'Unhide Post?';
    this.confirmationMessage = 'Are you sure you want to unhide this post?';
    this.confirmationAction = 'UnHide';
    this.showConfirmation = true;
    this.post_id = post_id;
    this.type = 'post';
  }

  public hidePost() {
    this.postService.HidePost(this.token, this.post_id).subscribe({
      next: (res) => {
        
        let index = this.posts.findIndex((p: Post) => p.id == this.post_id);
        if (index === -1) return;

        this.posts[index].isHidden = res.post.isHidden;
        this.CancelAction()
      },
    })
  }

  public CheckReport() {
    this.type = 'user';
    this.showConfirmation = true;
    this.DoYouWantReport = false;
    this.DoYouWantReportPost = false;
  }

  public Cancel() {
    this.DoYouWantReportPost = false;
    this.Report_Description = '';
    this.DoYouWantReport = false;
    this.description = '';
    this.confirmationTitle = 'Delete Post?';
    this.confirmationMessage = 'Are you sure you want to delete this post? This action cannot be undone.';
    this.confirmationAction = 'Delete';
  }

  public Report() {
    this.userService.Report(this.user.username, this.description, this.token).subscribe({
      next: () => {
        this.Cancel();
      },
    })
  }

  CheckDeletePost(post_id: number) {
    this.post_id = post_id;
    this.confirmationTitle = "Delete Post?";
    this.confirmationMessage = "Are you sure you want to delete this post?";
    this.confirmationAction = "Delete";
    this.showConfirmation = true;
    this.type = 'post';
  }

  CancelAction() {
    this.showConfirmation = false;
    this.post_id = null;
    this.description = '';
    this.confirmationTitle = 'Delete Post?';
    this.confirmationMessage = 'Are you sure you want to delete this post? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.type = '';
    this.Report_Description = '';
  }

  HandleAction(value: boolean) {
    if (!value) {
      this.CancelAction()
    } else if (this.type === 'post') {
      if (this.confirmationAction === "Delete") {
        this.deletePost()
      } else if (this.confirmationAction === "Hide") {
        this.hidePost();
      } else if (this.confirmationAction === "UnHide") {
        this.unhidePost();
      } else if (this.confirmationAction === "Report") {
        this.ReportPost();
        this.CancelAction();
      }
    } else if (this.type === 'user') {
      if (this.confirmationAction === "Report") {
        this.Report()
        this.Cancel()
        this.CancelAction()
      } else if (this.confirmationAction === "Remove") {
        this.RemoveUser()
        this.Cancel()
        this.CancelAction()
      } else if (this.confirmationAction === "Ban") {
        this.BanUser()
        this.Cancel()
        this.CancelAction()
      } else if (this.confirmationAction === "UnBan") {
        this.UnBanUser();
        this.Cancel();
        this.CancelAction();
      }
    }
  }

  public Report_Description: string = '';
  private type: string = '';

  OpenPostReportSection(post_id: number) {
    this.post_id = post_id;
    this.DoYouWantReportPost = true;
    this.DoYouWantReport = false;
  }

  CheckBeforeReport() {
    if (this.Report_Description.trim() === '') {
      this.toast.showError('Please provide a description for the report.', 3000);
      return;
    }

    if (this.Report_Description.length > 500) {
      this.toast.showError('Please provide a description for the report that is less than 500.', 3000);
      return;
    }
    this.showConfirmation = true;
    this.DoYouWantReportPost = false;
    this.confirmationAction = "Report";
    this.confirmationMessage = "Are you sure you want to report this post ? This action cannot be undone."
    this.confirmationTitle = `Report Post ?`;
    this.type = 'post';
  }


  public ReportPost(): void {
    this.setToken();
    this.postService.ReportPost(this.token, this.post_id, this.Report_Description).subscribe({
      next: () => {
        this.CancelAction()
      },
    })
  }

  public unhidePost() {
    this.postService.UnhidePost(this.token, this.post_id).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id == this.post_id);
        if (index === -1) return;

        this.posts[index].isHidden = res.post.isHidden;
        this.CancelAction()
      },
    })
  }


  public RemoveUser() {
    this.userService.RemoveUser(this.user.username, this.token).subscribe({
      next: () => {
        this.router.navigate(["admin"])
      },
    })
  }

  public UnBanUser() {
    this.setToken()
    this.userService.UnBanUserr(this.user.username, this.token).subscribe({
      next: () => {
        this.user.isbaned = !this.user.isbaned;
      },
    })
  }

  public BanUser() {
    this.setToken()
    this.userService.BanUserr(this.user.username, this.token).subscribe({
      next: () => {
        this.user.isbaned = !this.user.isbaned;
      },
    })
  }

  public setToken() {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    this.token = localStorage.getItem("JWT");
  }

  public deletePost() {
    this.setToken();
    this.postService.deletePost(this.token, this.post_id).subscribe({
      next: () => {
        let index = this.posts.findIndex((p: Post) => p.id === this.post_id)
        if (index === -1) return;
        this.posts.splice(index, 1)
        this.lastPost = this.posts[this.posts.length - 1];
        this.CancelAction()
        if (this.posts.length < 10) {
          this.LoadProfile()
        }
      },
    })
  }

  public React(post_id: number) {
    this.postService.React(this.token, post_id).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        if (index === -1) return;
        this.posts[index].likeCount = res.post.likeCount;
      },
    })
  }

  public ShowSinglePost(post_id: number): void {
    this.router.navigate([`post/${post_id}`])
  }

}
