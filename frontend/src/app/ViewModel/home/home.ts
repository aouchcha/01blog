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
import { Subject } from 'rxjs'; // Import Subject
import { takeUntil } from 'rxjs/operators'; // Import throttleTime
import { User } from '../../models/User';


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
  public media: File | null = null;
  public mediaName: string | null = null;
  public previewUrl: string | null = null;
  public isImage: boolean = false;
  public isVideo: boolean = false;
  public update: boolean = false;
  public post_id: number | null = null;
  public updatedMediaName: string | null = null;
  public updateMedia: File | null = null;
  updatePreviewUrl: string | null = null;
  updateIsImage: boolean = false;
  updateIsVideo: boolean = false;
  public oldMediaName: string | null = null;
  public oldMedia: File | null = null;
  public isBrowser: boolean = false;
  public Notifs: any = [];
  public notifsCount: number = 0;
  public ShowNotifs: boolean = false;
  public showConfirmation: boolean = false;
  public lastPost: Post | null | undefined = new Post();
  public isLoading: boolean = false;
  public HasMorePosts: boolean = true;
  public HasMoreUsers: boolean = true;
  public HasMoreNotifs: boolean = true;
  public Removed: boolean = false;
  private destroy$ = new Subject<void>();


  constructor(private router: Router, private postsService: PostsService, private userServise: UserService, @Inject(PLATFORM_ID) platformId: Object, private notifService: NotificationsService, private http: HttpClient, private state: ChangeDetectorRef) {
    this.isBrowser = isPlatformBrowser(platformId)
  }

  ngOnInit(): void {
    console.log({"message":"hchithalQ w dkhelt"});
    
    if (!this.isBrowser) {
      return
    }
    // this.media?.
    this.setToken()
    this.loadHome()

    this.notifService.notifications$.subscribe((notif) => {
      if (notif) {
        this.notifsCount = notif.count;
      }
    })

    this.notifService.reactionsObservable
      .pipe(takeUntil(this.destroy$))
      .subscribe(react => {
        let index = this.posts.findIndex((p: Post) => p.id === react.post.id);
        this.posts[index].likeCount = react.post.likeCount;
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.notifService.disconnect();
  }


  public setToken() {
    this.token = localStorage.getItem("JWT");
    console.log(this.token);
    
  }

  public loadHome(): void {
    this.getOthers();
    this.getMe();
    this.getAllPosts("feed");
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
    // this.previewUrl = null;
    // this.updatePreviewUrl = null;
    this.update = false;
    this.isImage = false;
    this.isVideo = false;
  }

  public Logout(): void {
    localStorage.removeItem("JWT");
    this.token = null;
    this.notifService.disconnect();
    this.router.navigate(["login"]);
  }

  public setMedia(event: Event, helper: string): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const reader = new FileReader();

    const isImage = file.type.startsWith("image/");
    const isVideo = file.type.startsWith("video/");

    reader.onload = () => {
      if (helper === 'create') {
        this.media = file;
        this.mediaName = file.name;

        this.previewUrl = reader.result as string;
        this.isImage = isImage;
        this.isVideo = isVideo;
      }

      else if (helper === 'update') {
        this.Removed = true;
        this.updateMedia = file;
        this.updatedMediaName = file.name;

        this.updatePreviewUrl = reader.result as string;
        // console.log({isImage});
        // console.log({isVideo});
        
        this.updateIsImage = isImage;
        this.updateIsVideo = isVideo;
      }
    };

    reader.readAsDataURL(file);
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
        this.posts = [];
        this.lastPost = null;
        this.HasMorePosts = true;
        this.previewUrl = null;
        this.HasMorePosts = true;
        this.getAllPosts("feed");
      },
      error: (err) => {
        console.log(err);
        if (err.status === 403) {
          this.router.navigate(["login"])
        }
      }
    })
  }



  public getAllPosts(helper: string): void {
    if (this.isLoading) return;

    this.setToken();
    this.isLoading = true;
    this.ShowNotifs = false;
    if (helper === "feed") {
      this.lastPost = null;
      this.lastNotif = null;
      this.posts = [];
      this.HasMoreNotifs = true;
      this.Notifs = [];
    }

    this.postsService.getAllPosts(this.token, this.lastPost).subscribe({
      next: (res) => {

        if (res.posts && res.posts.length > 0) {
          this.posts = [...this.posts, ...res.posts];
          this.lastPost = this.posts[this.posts.length - 1];
        } else {
          this.HasMorePosts = false;
        }

        this.isLoading = false;
      },
      error: (err) => {
        console.log(err);
        this.isLoading = false;
        if (err.status == 401) {
          this.Logout();
        }
      }
    });
  }

  public handleScrollLogic(event: any, helper: string): void {
    const element = event.target;

    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 50;
    if (helper == 'posts') {
      if (atBottom && !this.isLoading && this.HasMorePosts) {
        this.getAllPosts("other");
      }
    } else if (helper == 'users') {
      if (atBottom && !this.isLoading && this.HasMoreUsers) {
        this.getOthers();
      }
    } else if (helper == 'notifs') {
      if (atBottom && !this.isLoading && this.HasMoreNotifs) {
        this.getNotif();
      }
    }
  }

  public getMe(): void {
    console.log({"TTTTTTTTTt":this.token});
    
    this.userServise.getMe(this.token).subscribe({
      next: (res) => {
        console.log({res});
        
        this.me = res.me;
        // this.token = 
        this.notifService.connect(this.me.id, this.token)
        this.notifsCount = res.notifCount;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public lastUserId: number | null = null;

  public getOthers(): void {
    if (this.isLoading) return;
    this.userServise.getOtherUsers(this.token, this.lastUserId).subscribe({
      next: (res) => {
        if (res.users && res.users.length > 0) {
          this.others = [...this.others, ...res.users];
          this.lastUserId = this.others[this.others.length - 1].id;
          this.isLoading = false;
          console.log(this.others.length);

        } else {
          this.HasMoreUsers = false;
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.log(err);
      }
    })
  }

  public ShowUpdate(post_id: number): void {
    this.update = true;
    this.post_id = post_id;
    const post: Post = this.posts.find((p: Post) => p.id === post_id);

    if (!post) return;

    this.updatedTitle = post.title;
    this.updatedDescription = post.description;
    if (post.media && post.mediaUrl) {
      this.updateMedia = post.media;
      this.oldMedia = post.media;
      this.updatedMediaName = post.mediaUrl.substring(30);
      this.oldMediaName = post.mediaUrl.substring(30);
      // console.log({"old media": this.oldMediaName});
      this.updatePreviewUrl = post.mediaUrl;
      this.VideoOrImage(this.oldMediaName);
      // this.updateIsImage = true;
      // this.old
    }
  }

  public VideoOrImage(Url: string) {
    if (Url.endsWith(".mp4")) {
      this.updateIsVideo = true;
      this.updateIsImage = false;
    }else {
      this.updateIsImage = true;
      this.updateIsVideo = false;
    }
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
    this.postsService.updatePost(this.token, this.post_id, data, this.Removed).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.posts[index] = res.post;
        this.Removed = false;
        this.Cancel()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  RemoveImage(helper: string) {
    if (helper === 'create') {
      this.media = null;
      this.mediaName = "";
      this.previewUrl = null;
      this.isImage = false;
      this.isVideo = false;
    }
    else if (helper === 'edit') {
      this.updateMedia = null;
      this.updatedMediaName = "";
      this.updatePreviewUrl = null;
      this.updateIsImage = false;
      this.updateIsVideo = false;
      this.Removed = true;
    }
  }


  public confirmationTitle: string = 'Delete Post?';
  public confirmationMessage: string = 'Are you sure you want to delete this post? This action cannot be undone.';
  public confirmationAction: string = 'Delete';

  CheckConfirmation(post_id: number) {
    this.post_id = post_id;
    this.showConfirmation = true;
  }

  public deletePost() {
    this.setToken();
    this.postsService.deletePost(this.token, this.post_id).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.posts.splice(index, 1)
        this.lastPost = this.posts[this.posts.length - 1];
        console.log({"REMOVE LAST":this.lastPost});
        
        if (this.posts.length < 10) {
          this.getAllPosts('other')
        }
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
      this.CancelAction()
    } else {
      this.deletePost()
    }
  }

  public React(post_id: number): void {
    this.setToken();
    this.postsService.React(this.token, post_id).subscribe({
      next: () => {
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public Follow(user_id: number): void {
    this.setToken();
    this.userServise.Follow(user_id, this.token).subscribe({
      next: (res) => {
        console.log({ res });
        this.lastUserId = null;
        this.HasMoreUsers = true;
        this.others = [];
        // let index = this.others.findIndex((u: User) => u.username === res.user.username);
        // this.others[index] = res.user;
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
    this.router.navigate([`user/${username}`])
  }

  public lastNotif: any = null;

  public getNotif() {
    this.ShowNotifs = true;
    if (this.isLoading) return;

    this.notifService.getNotifs(this.token, this.lastNotif).subscribe({
      next: (res) => {
        if (res.notifs && res.notifs.length > 0) {
          this.Notifs = [...this.Notifs, ...res.notifs];
          this.lastNotif = this.Notifs[this.Notifs.length - 1];
          this.isLoading = false;
          console.log(this.Notifs.length);
          
        } else {
          this.HasMoreNotifs = false;
        }
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public CloseNotif() {
    this.ShowNotifs = false;
    this.isLoading = false;
    this.lastNotif = null;
    this.Notifs = [];
    this.HasMoreNotifs = true;
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
    this.notifService.markAsRead(this.token, notification_id).subscribe({
      next: (res) => {
        this.notifsCount = this.notifsCount > 0 ? this.notifsCount - 1 : 0;
        let index = this.Notifs.findIndex((notif: any) => notif.id === notification_id);
        this.Notifs[index] = res.notification;
      },
      error: (err) => {
        console.log(err);
      }
    })

  }

}
