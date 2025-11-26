import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule, isPlatformBrowser } from '@angular/common';
// import { StateService } from '../helpers/state-service';
import { generateURL, generateHeader, CheckToken } from '../../helpers/genarateHeader';
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
import { log } from 'console';
import { Subject, takeUntil } from 'rxjs';
// import { Comment } from '../helpers/Comments';

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
  // public post_id: number | null = null;
  public updatedTitle: string = '';
  public updatedDescription: string = '';
  public updatedMediaName: String | null = null;
  public updateMedia: File | null = null;
  public oldMediaName: string | null = null;
  public oldMedia: File | null = null;
  public isBrowser: boolean = false;
  public comment_id: number | null = null;
  public type: string = '';
  private destroy$ = new Subject<void>();



  constructor(private router: Router, private http: HttpClient, private postsService: PostsService, private notifService: NotificationsService, private route: ActivatedRoute, private userService: UserService, @Inject(PLATFORM_ID) platformId: Object) {
    // this.postsService = postsService
    this.isBrowser = isPlatformBrowser(platformId)

  }

  ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }
    if (!CheckToken()) {
      localStorage.removeItem("JWT")
      this.router.navigate(["login"])
    }
    this.token = CheckToken();

  this.notifService.reactionsObservable
    .pipe(takeUntil(this.destroy$))
    .subscribe(react => {
      console.log("single post react", react);
      this.post.likeCount = react.post.likeCount
    });
    
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
        // console.log({res});

        this.me = res.me;
        this.notifService.connect(this.me.id)

      },
      error: (err) => {
        console.log(err);
      }
    })
    this.getPost()
  }

  public getPost() {
    this.postsService.getSinglePost(this.token, this.post_id).subscribe({
      next: (res) => {
        console.log({ res });
        this.post = res.post
        this.comments = res.comments
      },
      error: (err) => {
        console.log(err);
        if (err.status === 401) {
          this.router.navigate(["login"])
        }
      }
    })
  }

  public Comment() {
    this.postsService.CreateComment(this.content, this.post_id, this.token).subscribe({
      next: (res) => {
        this.getPost()
        this.content = '';
      },
      error: (err) => {
        console.log(err);
      },
    })
  }

  public ShowUpdate(): void {
    console.log("hanni");

    this.update = true;
    // this.post_id = post_id;
    // const post: Post = this.posts.find((p: Post) => p.id === post_id);

    // if (!post) return;

    this.updatedTitle = this.post.title;
    this.updatedDescription = this.post.description;
    if (this.post.media && this.post.mediaUrl) {
      this.updateMedia = this.post.media;
      this.oldMedia = this.post.media;
      this.updatedMediaName = this.post.mediaUrl.substring(30);
      this.oldMediaName = this.post.mediaUrl.substring(30);
    }
  }

  public setMedia(event: Event, helper: String): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {

      this.Removed = true;
      this.updateMedia = input.files[0];
      this.updatedMediaName = input.files[0].name;

    }
  }

  public Cancel(): void {
    this.update = false;
    this.updatedTitle = '';
    this.updatedDescription = '';
    this.updateMedia = null;
    this.updatedMediaName = null;
  }

  public updatePost() {
    console.log({ "update alkhra": "" });
    const data = new FormData();
    data.append("title", this.updatedTitle);
    data.append("description", this.updatedDescription);

    console.log({ "UpdatedMedia": this.updateMedia });
    // console.log({ "OldMedia": this.oldMedia });
    // console.log({ "UpdatedMediaName": this.updatedMediaName });
    // console.log({ "OldMediaName": this.oldMediaName });


    console.log(this.oldMedia === this.updateMedia);
    console.log(this.oldMediaName === this.updatedMediaName);


    if (this.updateMedia) {
      data.append("media", this.updateMedia)
    }
    this.postsService.updatePost(this.token, this.post_id, data, this.Removed).subscribe({
      next: (res) => {
        console.log(res.post);
        // let index = this.posts.findIndex((p: Post) => p.id === res.post.id)
        this.post = res.post;
        this.Removed = false;
        this.Cancel()
      },
      error: (err) => {
        console.log(err);
      }
    })
    // this.postsService.updatePost(this.token, post_id)
  }

  public Removed: boolean = false;


  public RemoveImage(type: string) {
    console.log("sssssssssssss");
    this.Removed = true;

    this.updateMedia = null;
    this.updatedMediaName = null;

  }

  public confirmationTitle: string = 'Delete Post?';
  public confirmationMessage: string = 'Are you sure you want to delete this post? This action cannot be undone.';
  public confirmationAction: string = 'Delete';
  public showConfirmation: boolean = false;

  CheckConfirmation() {
    // console.log(post_id)
    // this.post_id = post_id;
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
      next: (res) => {
        console.log(res);
        this.CancelAction()
        setTimeout(() => {
          // this.error = '';
          // this.stateup.detectChanges();
          this.router.navigate([""])
        }, 2000);
      },
      error: (err) => {
        console.log(err);
      }
    })

  }

  HandleAction(value: boolean) {
    if (!value) {
      console.log("hanni");
      this.CancelAction()
    } else {
      if (this.type === 'post') {
        this.deletePost()
      } else {
        this.deleteComment()
      }
    }
  }

  public React(post_id: number) {
    // console.log(post_id);
    // this.setToken();

    this.postsService.React(this.token, post_id).subscribe({
      next: (res) => {
        // console.log(res);
        // this.posts = res.posts;
        // this.stateup.detectChanges();
        // this.getPost()
        this.post.likeCount = res.post.likeCount
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public ConfirmCommentDelet(comment_id: number) {
    this.confirmationTitle = 'Delete Comment?';
    this.confirmationMessage = 'Are you sure you want to delete this commet? This action cannot be undone.';
    this.confirmationAction = 'Delete';
    this.showConfirmation = true;
    this.comment_id = comment_id;
  }

  public deleteComment() {
    this.postsService.DeleteComment(this.token, this.comment_id).subscribe({
      next: (res) => {
        console.log(res);
        let index = this.comments.findIndex((c: Comment) => c.id === this.comment_id)
        this.comments.splice(index, 1)
        this.post.commentsCount -= 1;
        this.CancelAction()
        // this.router.navigate([""])
      },
      error: (err) => {
        console.log(err);
      }
    })

  }


  public Home() {
    this.router.navigate([""])
  }
}
