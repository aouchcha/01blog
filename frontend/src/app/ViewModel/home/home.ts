import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Subject, takeUntil } from 'rxjs';

import { PostsService } from '../../services/posts.service';
import { UserService } from '../../services/user.service';
import { NotificationsService } from '../../services/notification.service';
import { ToastService } from '../../services/toast.service';

import { Post } from '../../models/Post';
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
  private token: String | null = '';
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
  public updatePreviewUrl: string | null = null;
  public updateIsImage: boolean = false;
  public updateIsVideo: boolean = false;
  public oldMediaName: string | null = null;
  public oldMedia: File | null = null;
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


  constructor(
    private router: Router,
    private postsService: PostsService,
    private userServise: UserService,
    private notifService: NotificationsService,
    private toast: ToastService,
  ) {  }

  ngOnInit(): void {
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
        if (index === -1) return;
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
    this.previewUrl = null;
    this.updatePreviewUrl = null;
    this.update = false;
    this.isImage = false;
    this.isVideo = false;
    this.DoYouWantReport = false;
    this.Report_Description = '';
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
      this.handleSelectedMedia(helper, file, reader.result as string, isImage, isVideo);
    };

    reader.readAsDataURL(file);
  }

  private handleSelectedMedia(helper: string, file: File, dataUrl: string, isImage: boolean, isVideo: boolean): void {
    if (helper === 'create') {
      this.media = file;
      this.mediaName = file.name;
      this.previewUrl = dataUrl;
      this.isImage = isImage;
      this.isVideo = isVideo;
      return;
    }

    // update flow
    this.Removed = true;
    this.updateMedia = file;
    this.updatedMediaName = file.name;
    this.updatePreviewUrl = dataUrl;
    this.updateIsImage = isImage;
    this.updateIsVideo = isVideo;
  }


  public CreatePost(): void {
    if (this.title.trim() === '' || this.description.trim() === '') {
      this.toast.showError('Title and Description are required.', 3000);
      return;
    }

    if (this.title.length > 255) {
      this.toast.showError('Title cannot exceed 255 characters.', 3000);
      return;
    }

    if (this.description.length > 1000) {
      this.toast.showError('Description cannot exceed 1000 characters.', 3000);
      return;
    }

    this.setToken();
    const data = new FormData();
    data.append("title", this.title);
    data.append("description", this.description);
    if (this.media) {
      data.append("media", this.media);
    }

    this.postsService.CreatePost(this.token, data).subscribe({
      next: () => {
        this.Cancel();
        this.posts = [];
        this.lastPost = null;
        this.HasMorePosts = true;
        this.previewUrl = null;
        this.getAllPosts('feed');
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
      error: () => {
        this.isLoading = false;
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
    this.userServise.getMe(this.token).subscribe({
      next: (res) => {
        this.me = res.me;
        if (this.me.role === "Admin") {
          this.toast.showError('Admins do not have access to home.', 3000);
          this.Logout();
          return;
        };

        this.notifService.connect(this.me.id, this.token)
        this.notifsCount = res.notifCount;
      },
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
        } else {
          this.HasMoreUsers = false;
        }
      },
      error: () => {
        this.isLoading = false;
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
      this.updateMedia = null;
      this.oldMedia = null;
      this.updatedMediaName = post.mediaUrl.substring(30);
      this.oldMediaName = post.mediaUrl.substring(30);
      this.updatePreviewUrl = post.mediaUrl;
      this.VideoOrImage(this.oldMediaName);
    }
  }

  public VideoOrImage(Url: string) {
    if (Url.endsWith(".mp4")) {
      this.updateIsVideo = true;
      this.updateIsImage = false;
    } else {
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

    // Only append media if it's a File (user selected a new file).
    if (this.updateMedia && this.updateMedia instanceof File) {
      data.append("media", this.updateMedia);
    }
    this.postsService.updatePost(this.token, this.post_id, data, this.Removed).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.posts[index] = res.post;
        this.Removed = false;
        this.Cancel()
      },
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
    this.confirmationTitle = 'Delete Post?';
    this.confirmationMessage = 'Are you sure you want to delete this post? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.post_id = post_id;
    this.showConfirmation = true;
  }

  public DoYouWantReport: boolean = false;
  public Report_Description: string = '';

  OpenReportSection(post_id: number) {
    this.post_id = post_id;
    this.DoYouWantReport = true;
  }

  CheckBeforeReport() {
    if (this.Report_Description.trim() === '') {
      this.toast.showError('Please provide a description for the report.', 3000);
      return;
    }
    this.showConfirmation = true;
    this.DoYouWantReport = false;
    this.confirmationAction = "Report";
    this.confirmationMessage = "Are you sure you want to report this post ? This action cannot be undone."
    this.confirmationTitle = `Report Post ?`;

  }

  public deletePost() {
    this.setToken();
    this.postsService.deletePost(this.token, this.post_id).subscribe({
      next: (res) => {
        let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        if (index === -1) return;
        this.posts.splice(index, 1)
        this.lastPost = this.posts[this.posts.length - 1];
        if (this.posts.length < 10) {
          this.getAllPosts('other')
        }
        this.CancelAction()
      },
    })
  }

  CancelAction() {
    this.Report_Description = '';
    this.DoYouWantReport = false;
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
        this.ReportPost()
      }
    }
  }

  public ReportPost(): void {
    this.setToken();
    this.postsService.ReportPost(this.token, this.post_id, this.Report_Description).subscribe({
      next: () => {
        this.CancelAction()
      },
    })
  }

  public React(post_id: number): void {
    this.setToken();
    this.postsService.React(this.token, post_id).subscribe({})
  }

  public Follow(user_id: number): void {
    this.setToken();
    this.userServise.Follow(user_id, this.token).subscribe({
      next: () => {
        this.lastUserId = null;
        this.HasMoreUsers = true;
        this.others = [];
        this.getOthers()
      },
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
        if (res.notifications && res.notifications.length > 0) {
          this.Notifs = [...this.Notifs, ...res.notifications];
          this.lastNotif = this.Notifs[this.Notifs.length - 1];
          this.isLoading = false;
        } else {
          this.HasMoreNotifs = false;
        }
      },
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
        if (index === -1) return;
        this.Notifs[index] = res.notification;
      },
    })

  }

}
