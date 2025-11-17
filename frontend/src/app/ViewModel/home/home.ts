import { ChangeDetectorRef, Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { CheckToken } from '../../helpers/genarateHeader';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms'
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PostsService } from '../../services/posts.service';
import { UserService } from '../../services/user.service';
import { NotificationsService } from '../../services/notification.service';
import { Post } from '../../models/Post';
import { HttpClient } from '@angular/common/http';
import { Confirmation } from '../confirmation/confirmation';


@Component({
  selector: 'app-home',
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
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  public title: string = '';
  public description: string = '';
  public updatedTitle: string = '';
  public updatedDescription: string = '';
  public token: String | null = '';
  public posts: any = [];
  public me: any = {};
  public others: any = [];
  public mediaName: string | null = null;
  public media: File | null = null;
  public update: boolean = false;
  public post_id: number | null = null;
  public updatedMediaName: string | null = null;
  public updateMedia: File | null = null;
  public isBrowser: boolean = false;
  public Notifs: any = [];
  public notifsCount: number = 0;
  public ShowNotifs: boolean = false;
  public showConfirmation: boolean = false;

  constructor(private router: Router, private postsService: PostsService, private userServise: UserService, @Inject(PLATFORM_ID) platformId: Object, private notifService: NotificationsService, private http: HttpClient, private state: ChangeDetectorRef) {
    this.isBrowser = isPlatformBrowser(platformId)
  }

  ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }

    this.setToken()
    this.loadHome()

    this.notifService.notifications$.subscribe((notif) => {
      if (notif) {
        this.notifsCount = notif.count;
      }
    })

    this.notifService.reactionsObservable.subscribe((react) => {
      if (react) {
        let index = this.posts.findIndex((p: Post) => p.id === react.post.id)
        this.posts[index] = react.post;
      }
    })

  }

  public setToken() {
    // if (CheckToken() === null) {
    //   this.router.navigate(["login"]);
    //   return;
    // }
    this.token = localStorage.getItem("JWT");
  }

  public loadHome(): void {
    this.getOthers();
    this.getMe();
    this.getAllPosts();
  }

  public Cancel(): void {
    this.title = '';
    this.description = '';
    this.mediaName = null;
    this.media = null;
    this.update = false;
    this.updatedTitle = '';
    this.updatedDescription = '';
    this.updateMedia = null;
    this.updatedMediaName = null;
    this.post_id = null;
  }

  public Logout(): void {
    localStorage.removeItem("JWT");
    this.token = null;
    this.router.navigate(["login"]);
    // this.http.get(`http://localhost:8080/api/notifications/disconnect/${this.me.id}`).subscribe({
    //   next: () => this.notifService.disconnect(),
    //   complete: () => this.router.navigate(["login"])
    // });
  }

  public setMedia(event: Event, helper: String): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      if (helper === 'create') {
        this.media = input.files[0];  // store the file itself
        this.mediaName = input.files[0].name;
      } else if (helper === 'update') {
        this.updateMedia = input.files[0];
        this.updatedMediaName = input.files[0].name;
      }
    }
  }

  public CreatePost(): void {
    this.setToken();
    const data = new FormData();
    data.append("title", this.title);
    data.append("description", this.description);
    if (this.media) {
      data.append("media", this.media)
    }

    this.postsService.CreatePost(this.token, data).subscribe({
      next: () => {
        this.Cancel()
        this.getAllPosts();
      },
      error: (err) => {
        console.log(err);
        if (err.status === 403) {
          this.router.navigate(["login"])
        }
      }
    })
  }

  public getAllPosts(): void {
    this.setToken();
    this.ShowNotifs = false;
    this.postsService.getAllPosts(this.token).subscribe({
      next: (res) => {
        console.log(res.posts);

        this.posts = res.posts;
      },
      error: (err) => {
        console.log(err);
        if (err.status == 401) {
          this.Logout()
        }

      }
    })
  }

  public getMe(): void {
    this.userServise.getMe(this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.me = res.me;
        this.notifService.connect(this.me.id)
        this.notifsCount = res.notifCount;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public getOthers(): void {
    this.userServise.getOtherUsers(this.token).subscribe({
      next: (res) => {
        console.log("all users ========", res);
        this.others = res.users;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public ShowUpdate(post_id: number): void {
    console.log("hanni");

    this.update = true;
    this.post_id = post_id;
    const post: Post = this.posts.find((p: Post) => p.id === post_id);

    if (!post) return; // avoid undefined errors

    this.updatedTitle = post.title;
    this.updatedDescription = post.description;
    this.updateMedia = post.media;
    this.updatedMediaName = post.mediaUrl.substring(30);
  }

  public UpdatePost() {
    this.setToken();
    if (!this.post_id) return;
    const data = new FormData();
    data.append("title", this.updatedTitle);
    data.append("description", this.updatedDescription);
    if (this.updateMedia) {
      data.append("media", this.updateMedia)
    }
    this.postsService.updatePost(this.token, this.post_id, data).subscribe({
      next: (res) => {
        console.log(res.post);
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.posts[index] = res.post;
        this.Cancel()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public confirmationTitle: string = 'Delete Post?';
  public confirmationMessage: string = 'Are you sure you want to delete this post? This action cannot be undone.';
  public confirmationAction: string = 'Delete';

  CheckConfirmation(post_id: number) {
    console.log(post_id)
    this.post_id = post_id;
    this.showConfirmation = true;
  }

  public deletePost() {
    this.setToken();
    this.postsService.deletePost(this.token, this.post_id).subscribe({
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

  CancelAction() {
    this.showConfirmation = false;
    this.post_id = null;
  }

  HandleAction(value: boolean) {
    if (!value) {
      console.log("hanni");
      this.CancelAction()
    } else {
      
      this.deletePost()
    }
  }

  public React(post_id: number): void {
    this.setToken();
    this.postsService.React(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
        // let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        // this.posts[index] = res.post;
        // this.getAllPosts()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public Follow(user_id: number): void {
    console.log("zebbi");
    this.setToken();
    this.userServise.Follow(user_id, this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.getOthers()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public ShowSinglePost(post_id: number): void {
    this.router.navigate([`post/${post_id}`])
  }

  public ToProfile(username: String) {
    console.log({ username });
    this.router.navigate([`user/${username}`])
  }

  public ShowNotif() {
    this.notifService.getNotifs(this.token).subscribe({
      next: (res) => {
        console.log({ res });
        this.Notifs = res.notifs;
        this.ShowNotifs = true;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public CloseNotif() {
    this.ShowNotifs = false;
  }

  leftMenuOpen = false;
  rightMenuOpen = false;

  toggleLeftMenu() {
    this.leftMenuOpen = !this.leftMenuOpen;
    this.rightMenuOpen = false;
  }

  toggleRightMenu() {
    this.rightMenuOpen = !this.rightMenuOpen;
    this.leftMenuOpen = false;
  }

  closeMenus() {
    this.leftMenuOpen = false;
    this.rightMenuOpen = false;
  }

  public MarkNotifsAsRead(notification_id: number): void {
    console.log({notification_id});
    this.notifService.markAsRead(this.token, notification_id).subscribe({
      next: (res) => {
        console.log(res);
        this.notifsCount = this.notifsCount > 0 ? this.notifsCount - 1 : 0;
        let index = this.Notifs.findIndex((notif: any) => notif.id === notification_id);
        this.Notifs[index] = res.notification;
        console.log({"NOOOOOOO": this.Notifs[index]});
        
      },
      error: (err) => {
        console.log(err);
      }
    })
    
  }

}
