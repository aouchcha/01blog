import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
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
  public isBrowser = false;
  constructor(private router: Router, private postsService: PostsService, private userServise: UserService, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId)
  }

  ngOnInit(): void {
    if (!this.isBrowser) {
      return
    }
    this.setToken()
    this.loadHome()

  }

  public setToken() {
    if (CheckToken() === null) {
      this.router.navigate(["login"]);
      return;
    }
    this.token = localStorage.getItem("JWT");
  }

  public loadHome(): void {
    this.getOthers();
    this.getMe();
    this.getAllPosts();
  }

  // public getDescription(): String {
  //   return this.description;
  // }

  public Cancel(): void {
    this.description = '';
    this.mediaName = null;
    this.media = null;
  }

  public Logout(): void {
    localStorage.removeItem("JWT");
    this.token = null;
    this.router.navigate(["login"])
  }

  public setMedia(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.media = input.files[0];  // store the file itself
      this.mediaName = input.files[0].name;
    }
  }

  public CreatePost(): void {
    this.setToken();
    const data = new FormData();
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
    this.postsService.getAllPosts(this.token).subscribe({
      next: (res) => {
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

  public updatePost(post_id: number) {
    this.postsService.updatePost(this.token, post_id)
  }

  public deletePost(post_id: number) {
    this.setToken();
    this.postsService.deletePost(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
      },
      error: (err) => {
        console.log(err);
      }
    })
    
  }

  public React(post_id: number): void {
    this.setToken();
    console.log("post id ========= ", post_id);
    this.postsService.React(this.token, post_id).subscribe({
      next: (res) => {
        console.log(res);
        this.getAllPosts()
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
    this.router.navigate([`/${username}`])
  }
}
