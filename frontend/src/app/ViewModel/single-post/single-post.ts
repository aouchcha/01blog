import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
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
    FormsModule
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
  public updatedMediaName: String | null = null;
  public updateMedia: File | null = null;

  constructor(private router: Router, private http: HttpClient, private postsService: PostsService, private route: ActivatedRoute, private userService: UserService) {
    this.postsService = postsService
  }

  ngOnInit(): void {
    if (!CheckToken()) {
      localStorage.removeItem("JWT")
      this.router.navigate(["login"])
    }
    this.token = CheckToken();
    this.post_id = Number(this.route.snapshot.paramMap.get('id'));
    this.LoadPage()
  }

  public LoadPage() {
    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        this.me = res.me;
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
        console.log(res);
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
  public updatePost(post_id: number) {
    // this.postsService.updatePost(this.token, post_id)
  }

  public deletePost(post_id: number) {
    this.postsService.deletePost(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
        this.router.navigate([""])
      },
      error: (err) => {
        console.log(err);
      }
    })

  }
  public React(post_id: number) {
    console.log(post_id);

    this.postsService.React(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
        // this.posts = res.posts;
        // this.stateup.detectChanges();
        this.getPost()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

   public deleteComment(comment_id: number) {
    this.postsService.DeleteComment(this.token, comment_id).subscribe({
      next: (res) => {
        console.log(res);
        let index = this.comments.findIndex((c: Comment) => c.id === comment_id)
        this.comments.splice(index, 1)
        this.post.commentsCount -= 1;
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
