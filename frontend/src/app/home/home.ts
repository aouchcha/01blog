import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { generateHeader, generateURL, CheckToken } from '../helpers/genarateHeader';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
// import { StateService } from '../helpers/state-service';
import { PostsService } from '../PostsService';
import { UserService } from '../UserService';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    FormsModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  public description: string = '';
  private token: String | null = '';
  public posts: any = [];
  public me: any = {};
  public others: any = [];
  public mediaName: String | null = null;
  public media: File | null = null;
  public type: String = 'image';
  constructor(private http: HttpClient, private router: Router, private postsService: PostsService, private userServise: UserService) { }

  ngOnInit(): void {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    console.log("hanni");
    this.token = localStorage.getItem("JWT");
    this.loadHome()

  }

  public loadHome(): void {
    this.getOthers();
    this.getMe();
    this.getAllPosts();
  }

  public getDescription(): String {
    return this.description;
  }

  public Cancel(): void {
    this.description = '';
    this.mediaName = null;
    this.media = null;
  }

  public Logout(): void {
    localStorage.removeItem("JWT");
    this.token = null;
    // this.router.navigate(["login"])
  }

  public setMedia(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.media = input.files[0];  // store the file itself
      this.mediaName = input.files[0].name;
    }
  }

  public CreatePost(): void {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    const data = new FormData();
    data.append("description", this.description);
    if (this.media) {
      data.append("media", this.media)
    }
    this.http.post<any>(
      generateURL("post"),
      data,
      generateHeader(this.token)
    ).subscribe({
      next: () => {
        console.log("ranni");
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

  // public CheckToken(): boolean {

  //   const Token = localStorage.getItem("JWT");
  //   if (!Token) {
  //     return false;
  //   }
  //   this.token = Token;
  //   return true;
  // }

  public getAllPosts(): void {
    this.postsService.getAllPosts(this.token).subscribe({
      next: (res) => {
        res.posts.forEach((p: any) => {
          console.log(p);
        });
        this.posts = res.posts;
      },
      error: (err) => {
        console.log(err);
        if (err.status == 401) {
          this.router.navigate(["login"])
        }

      }
    })
  }

  public getMe(): void {
    this.userServise.getMe(this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.me = res.me;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public getOthers(): void {
    this.http.get<any>(
      generateURL("users"),
      generateHeader(this.token)
    ).subscribe({
      next: (res) => {
        console.log("all users ========", res);
        this.others = res.users;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public updatePost(post: any) {
    this.postsService.updatePost(post)
  }

  public deletePost(post: any) {
    this.postsService.deletePost(post)
  }

  public React(post_id: number): void {
    console.log("post id ========= ", post_id);
    this.postsService.React(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
        // this.posts = res.posts;
        // this.stateup.detectChanges();
        this.getAllPosts()
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public Follow(user_id: number): void {
    console.log("zebbi");

    this.http.post<any>(
      generateURL("follow"),
      {
        "followed_id": user_id
      },
      generateHeader(this.token)
    ).subscribe({
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
    this.router.navigate([`/${username}`])
  }
}
