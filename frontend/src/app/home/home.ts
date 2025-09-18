import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { generateHeader, generateURL } from '../helpers/genarateHeader';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';


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
  public me : any = {};
  public mediaName: String | null = null;
  public media: File | null = null;
  public type: String = 'image';
  constructor(private http: HttpClient, private router: Router, private stateup: ChangeDetectorRef) { }

  ngOnInit(): void {
    if (!this.CheckToken()) {
      this.router.navigate(["login"]);
      return;
    }
    console.log("hanni");
    
    this.getAllPosts();
    this.getMe();
  }

  // public setDescription(): void {
  //   // console.log(description);

  //   this.description = "e";
  // }

  public getDescription(): String {
    return this.description;
  }

  public Cancel():void {

    this.description = '';
    this.mediaName = null;
    this.media = null;
  }

  public setMedia(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.media = input.files[0];  // store the file itself
      this.mediaName = input.files[0].name;
    }
    console.log('Selected file:', this.media);
  }

  public CreatePost(): void {
    if (!this.CheckToken()) {
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
        next: (res) => {
          console.log("ranni");
          this.Cancel()
          this.getAllPosts();
        },
        error: (err) => {
          console.log(err);
          // this.router.navigate(["login"])
        }
      })
  }

  public CheckToken(): boolean {
    
    const Token = localStorage.getItem("JWT");
    if (!Token) {
      return false;
    }
    this.token = Token;
    return true;
  }

  public getAllPosts() {
    this.http.get<any> (
      generateURL("post"),
      generateHeader(this.token)
    ).subscribe({
      next: (res) => {
        res.posts.forEach((p: any) => {
          console.log(p);
        });
        this.posts = res.posts;
      },
      error: (err) => {
        console.log(err);
        this.router.navigate(["login"])

      }
    })
  }

  public getMe() {
    this.http.get<any>(
      generateURL("me"),
      generateHeader(this.token)
    ).subscribe({
      next: (res) => {
        console.log(res);
        this.me = res.me;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public updatePost(post: any) {
    console.log('Update', post);
  }

  public deletePost(post: any) {
    console.log('Delete', post);
  }
}
