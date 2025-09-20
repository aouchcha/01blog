import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { StateService } from '../helpers/state-service';
import { generateURL, generateHeader, CheckToken } from '../helpers/genarateHeader';
// import {CheckToken}

@Component({
  selector: 'app-single-post',
  imports: [],
  templateUrl: './single-post.html',
  styleUrl: './single-post.css'
})
export class SinglePost implements OnInit {
  public post_id: Number = 0;
  public token: string | null = null;
  public post: any = {};
  public me: any = {};

  constructor(private state: StateService, private router: Router, private http: HttpClient) { }

  ngOnInit(): void {
    if (!CheckToken()) {
      localStorage.removeItem("JWT")
      this.router.navigate(["login"])
    }
    this.token = localStorage.getItem("JWT")
    this.state.selectedPostId$.subscribe(p_id => {
      this.post_id = p_id
    })
    this.state.CurrentUsser$.subscribe (me => {
      this.me = me;
    })
    this.getPost()
  }

  public getPost() {
    this.http.get<any>(
      generateURL(`post/${this.post_id}`),
      generateHeader(this.token)
    ).subscribe({
      next: (res) => {
        console.log(res);
        this.post = res.post 
      },
      error: (err) => {
        console.log(err);
      }
    })
  }
  
}
