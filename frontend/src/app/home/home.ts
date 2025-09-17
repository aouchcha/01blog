import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { generateHeader, generateURL } from '../helpers/genarateHeader';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  private description: String = '';
  private token: String | null = '';
  public posts: any = [];
  constructor(private http: HttpClient, private router: Router, private stateup: ChangeDetectorRef) { }

  ngOnInit(): void {
    if (!this.CheckToken()) {
      this.router.navigate(["login"]);
      return;
    }
    
    this.getAllPosts();
  }

  public setDescription(description: String): void {
    console.log(description);

    this.description = description;
  }

  public getDescription(): String {
    return this.description;
  }

  public CreatePost(): void {
    if (!this.CheckToken()) {
      this.router.navigate(["login"]);
      return;
    }
    const body = {
      "description": this.description
    };
    this.http.post<any>(
      generateURL("post"),
      body,
      generateHeader(this.token)
    ).subscribe({
        next: (res) => {
          console.log("ranni");
          this.description = '';
          this.stateup.detectChanges();
          this.getAllPosts();
        },
        error: (err) => {
          console.log(err);
          
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
     if (!this.CheckToken()) {
      this.router.navigate(["login"]);
      return;
    }
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
      }
    })
  }
}
