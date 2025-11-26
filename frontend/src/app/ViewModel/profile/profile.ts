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
  public description: string = "";
  public isBrowser = false;
  // public popupMessage: String = '';
  public showPopup: boolean = false;
  public ShowNotifs: boolean = false;
  public showConfirmation: boolean = false;
  public confirmationTitle: string = 'Delete Post?';
  public confirmationMessage: string = 'Are you sure you want to delete this post? This action cannot be undone.';
  public confirmationAction: string | undefined = 'Delete';
  public post_id: number | null = null;
  private destroy$ = new Subject<void>();

  // public confirmationParams: any = {};

  constructor(private router: Router, private route: ActivatedRoute, private notifService: NotificationsService, private userService: UserService, @Inject(PLATFORM_ID) platformId: Object, private postService: PostsService) {
    this.isBrowser = isPlatformBrowser(platformId)
  }

  public ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }
    if (!CheckToken()) {
      localStorage.removeItem("JWT")
      this.router.navigate(["login"])
    }
    this.token = CheckToken();
    this.username = String(this.route.snapshot.paramMap.get('username'));



    this.getMe()
    this.LoadProfile()

    this.notifService.reactionsObservable
      .pipe(takeUntil(this.destroy$))
      .subscribe(react => {
      console.log("profile react", react);

        let index = this.posts.findIndex((p: Post) => p.id === react.post.id);
        this.posts[index].likeCount = react.post.likeCount;
      });

  }

  ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}

  public getMe() {
    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        console.log({ "me": res.me });

        this.me = res.me;
        this.notifService.connect(this.me.id)

      },
      error: (err) => {
        console.log(err);
      }
    })
  }


  public LoadProfile() {
    this.userService.getProfile(this.username, this.token).subscribe({
      next: (res) => {
        console.log({ res });
        this.user = res.user;
        // console.log({"ussssss":this.user});

        this.posts = res.posts;
        this.followers = res.followers
        this.followings = res.followings
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public Home() {
    console.log("home");

    this.router.navigate([""])
  }

  public ToDashBoard() {
    this.router.navigate(["admin"])
  }

  public Follow() {
    console.log("zebbi");

    this.userService.Follow(this.user.id, this.token).subscribe({
      next: (res) => {
        this.LoadProfile()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public OpenReportSection() {
    this.DoYouWantReport = true;
    this.confirmationAction = "Report";
    this.confirmationMessage = "Are you sure you want to report this user? This action cannot be undone."
    this.confirmationTitle = `Report User: ${this.user.username} ?`;
  }

  public CheckBeforeRemoving() {
    this.showConfirmation = true;
    this.confirmationAction = "Remove";
    this.confirmationMessage = "Are you sure you want to remove this user? This action cannot be undone."
    this.confirmationTitle = `Remove User: ${this.user.username} ?`;
  }

  public CheckBeforeBan() {
    this.showConfirmation = true;
    this.confirmationAction = "Ban";
    this.confirmationMessage = "Are you sure you want to Ban this user? This action cannot be undone."
    this.confirmationTitle = `Ban User: ${this.user.username} ?`;
  }

  public CheckReport() {
    this.showConfirmation = true;
    this.DoYouWantReport = false;
  }

  public Cancel() {
    this.DoYouWantReport = false;
    this.description = '';
    this.confirmationTitle = 'Delete Post?';
    this.confirmationMessage = 'Are you sure you want to delete this post? This action cannot be undone.';
    this.confirmationAction = 'Delete';
  }

  // public setDescription(description: string) : void {
  //   this.description = description
  // }

  public Report() {
    console.log("report", { name: this.user.username, description: this.description });
    this.userService.Report(this.user.username, this.description, this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.Cancel();
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  CheckDeletePost(post_id: number) {
    this.post_id = post_id;
    this.confirmationTitle = "Delete Post?";
    this.confirmationMessage = "Are you sure you want to delete this post?";
    this.confirmationAction = "Delete";
    this.showConfirmation = true;
  }

  CancelAction() {
    this.showConfirmation = false;
    this.post_id = null;
  }

  HandleAction(value: boolean) {
    if (!value) {
      this.CancelAction()
    } else {
      if (this.confirmationAction === "Delete") {
        this.deletePost()
      } else if (this.confirmationAction === "Report") {
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
      }
    }
  }


  public RemoveUser() {
    console.log("hanni");

    this.userService.RemoveUser(this.user.username, this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.router.navigate(["admin"])
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public BanUser() {
    this.setToken()
    this.userService.BanUserr(this.user.username, this.token).subscribe({
      next: (res) => {
        this.user.isbaned = !this.user.isbaned;
        console.log(res);
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  // public closePopup() {
  //   this.showPopup = false;
  //   // this.popupMessage = '';
  // }

  public setToken() {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    this.token = localStorage.getItem("JWT");
  }

  public deletePost() {
    console.log(this.post_id);

    this.setToken();
    this.postService.deletePost(this.token, this.post_id).subscribe({
      next: (res) => {
        console.log(res);
        let index = this.posts.findIndex((p: Post) => p.id === this.post_id)
        this.posts.splice(index, 1)
        this.CancelAction()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public React(post_id: number) {
    this.postService.React(this.token, post_id).subscribe({
      next: (res) => {
        console.log("Profile Result", res);
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.posts[index].likeCount = res.post.likeCount;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public ShowSinglePost(post_id: number): void {
    this.router.navigate([`post/${post_id}`])
  }

}
