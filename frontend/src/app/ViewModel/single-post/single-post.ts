import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CheckToken } from '../../helpers/genarateHeader';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PostsService } from '../../services/posts.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/User';
import { Post } from '../../models/Post';
import { FormsModule } from '@angular/forms';
import { Comment } from '../../models/Comments';
import { NotificationsService } from '../../services/notification.service';
import { Confirmation } from '../confirmation/confirmation';
import { Subject, takeUntil } from 'rxjs';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-single-post',
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
  templateUrl: './single-post.html',
  styleUrl: './single-post.css'
})

export class SinglePost implements OnInit {
  public post_id: number = 0;
  public token: string | null = "";
  public post: Post = new Post();
  public me: User = new User();
  public content: String = "";
  public comments: any = [];
  public update: boolean = false;
  public updatedTitle: string = '';
  public updatedDescription: string = '';
  public updatedMediaName: String | null = null;
  public updateMedia: File | null = null;
  public oldMediaName: string | null = null;
  public oldMedia: File | null = null;
  public comment_id: number | null = null;
  public type: string = '';
  private destroy$ = new Subject<void>();
  public updatePreviewUrl: string | null = null;
  public updateIsImage: boolean = false;
  public updateIsVideo: boolean = false;


  constructor(private router: Router, private postsService: PostsService, private notifService: NotificationsService, private route: ActivatedRoute, private userService: UserService, private toast: ToastService) { }

  ngOnInit(): void {
    this.token = CheckToken();

    if (this.me.role !== 'Admin') {
      this.notifService.reactionsObservable
        .pipe(takeUntil(this.destroy$))
        .subscribe(react => {
          this.post.likeCount = react.post.likeCount
        });
    }

    this.post_id = Number(this.route.snapshot.paramMap.get('id'));
    this.LoadPage()
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public LoadPage() {
    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        this.me = res.me;
        if (this.me.role !== "Admin") {
          this.notifService.connect(this.me.id, this.token)
        }

      },
    })
    this.getPost()
  }

  public getPost() {
    this.postsService.getSinglePost(this.token, this.post_id).subscribe({
      next: (res) => {
        this.post = res.post
        this.comments = res.comments
      },
    })
  }

  public Comment() {
    this.postsService.CreateComment(this.content, this.post_id, this.token).subscribe({
      next: (res) => {
        this.getPost()
        // console.log({res});
        
        // this.post = res.post
        this.content = '';
      },
    })
  }

  public ShowUpdate(): void {
    this.update = true;
    this.updatedTitle = this.post.title;
    this.updatedDescription = this.post.description;

    if (this.post.media && this.post.mediaUrl) {
      this.updateMedia = null;
      this.oldMedia = null;
      this.updatedMediaName = this.post.mediaUrl.substring(30);
      this.oldMediaName = this.post.mediaUrl.substring(30);
      this.updatePreviewUrl = this.post.mediaUrl;
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

  public setMedia(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const reader = new FileReader();

    const isImage = file.type.startsWith("image/");
    const isVideo = file.type.startsWith("video/");

    reader.onload = () => {
      this.updatePreviewUrl = reader.result as string;
      this.updateIsImage = isImage;
      this.updateIsVideo = isVideo;
      this.updateMedia = file;
      this.updatedMediaName = file.name;
    }

    this.Removed = true;
    reader.readAsDataURL(file);
  }

  public Cancel(): void {
    this.update = false;
    this.updatedTitle = '';
    this.updatedDescription = '';
    this.updateMedia = null;
    this.updatedMediaName = null;
  }

  public UpdatePost() {
    const data = new FormData();
    data.append("title", this.updatedTitle);
    data.append("description", this.updatedDescription);

    if (this.updateMedia && this.updateMedia instanceof File) {
      data.append("media", this.updateMedia);
    }
    this.postsService.updatePost(this.token, this.post_id, data, this.Removed).subscribe({
      next: (res) => {
        this.post = res.post;
        this.Removed = false;
        this.Cancel()
      },
    })
  }

  public Removed: boolean = false;


  public RemoveImage() {
    this.Removed = true;
    this.updateMedia = null;
    this.updatedMediaName = null;
    this.updatePreviewUrl = null;
  }

  public confirmationTitle: string = '';
  public confirmationMessage: string = '';
  public confirmationAction: string = '';
  public showConfirmation: boolean = false;

  CheckConfirmation() {
    this.confirmationTitle = 'Delete Post?';
    this.confirmationMessage = 'Are you sure you want to delete this post? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.showConfirmation = true;
    this.type = 'post';
    this.comment_id = null;
  }

  CheckBeforeHide() {
    this.confirmationTitle = 'Hide Post?';
    this.confirmationMessage = 'Are you sure you want to hide this post?';
    this.confirmationAction = 'Hide';
    this.showConfirmation = true;
    this.type = 'post';
    this.comment_id = null;
  }

  CheckBeforeUnHide() {
    this.confirmationTitle = 'UnHide Post?';
    this.confirmationMessage = 'Are you sure you want to unhide this post?';
    this.confirmationAction = 'UnHide';
    this.showConfirmation = true;
    this.type = 'post';
    this.comment_id = null;
  }

  CancelAction() {
    this.showConfirmation = false;
    this.comment_id = null;
  }




  public deletePost() {
    this.postsService.deletePost(this.token, this.post_id).subscribe({
      next: () => {
        this.CancelAction()
        setTimeout(() => {
          this.router.navigate([""])
        }, 2000);
      },
    })

  }

  public hidePost() {
    this.postsService.HidePost(this.token, this.post_id).subscribe({
      next: (res: any) => {
        this.post.isHidden = res.post.isHidden
        this.CancelAction()
      },
    })
  }

  public unhidePost() {
    this.postsService.UnhidePost(this.token, this.post_id).subscribe({
      next: (res: any) => {
        this.post.isHidden = res.post.isHidden
        this.CancelAction()
      },
    })
  }

  HandleAction(value: boolean) {
    if (!value) {
      this.CancelAction()
    } else {
      if (this.type === 'post') {
        if (this.confirmationAction === 'Hide') {
          this.hidePost()
        } else if (this.confirmationAction === 'Delete') {
          this.deletePost()
        } else if (this.confirmationAction === 'UnHide') {
          this.unhidePost()
        } else if (this.confirmationAction === 'Report') {
          this.ReportPost()
        }
      } else {
        this.deleteComment()
      }
    }
  }

  public React(post_id: number) {
    this.postsService.React(this.token, post_id).subscribe({

    })
  }

  public ConfirmCommentDelet(comment_id: number) {
    this.confirmationTitle = 'Delete Comment?';
    this.confirmationMessage = 'Are you sure you want to delete this commet? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.showConfirmation = true;
    this.type = 'comment';
    this.comment_id = comment_id;
  }

  public DoYouWantReport: boolean = false;
  public Report_Description: string = '';

  OpenReportSection(post_id: number) {
    this.post_id = post_id;
    this.comment_id = null;
    this.DoYouWantReport = true;
  }

  CheckBeforeReport() {
    if (this.Report_Description.trim() === '') {
      this.toast.showError('Please provide a description for the report.', 3000);
      return;
    }

    if (this.Report_Description.length > 500) {
      this.toast.showError('Please provide a description for the report be less than 500 letter.', 3000);
      return;
    }

    this.showConfirmation = true;
    this.DoYouWantReport = false;
    this.confirmationAction = "Report";
    this.confirmationMessage = "Are you sure you want to report this post ? This action cannot be undone."
    this.confirmationTitle = `Report Post ?`;
    this.type = 'post';
  }

  public ReportPost(): void {
    this.postsService.ReportPost(this.token, this.post_id, this.Report_Description).subscribe({
      next: () => {
        this.CancelAction()
      },
    })
  }

  public deleteComment() {
    this.postsService.DeleteComment(this.token, this.comment_id).subscribe({
      next: () => {
        let index = this.comments.findIndex((c: Comment) => c.id === this.comment_id)
        if (index === -1) return;
        this.comments.splice(index, 1)
        this.post.commentsCount -= 1;
        this.CancelAction()
      },
    })

  }


  public Home() {
    this.router.navigate([""])
  }

  public ToReports() {
    this.router.navigate(["/reports"])
  }
}
