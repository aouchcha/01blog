import { Component, OnInit } from '@angular/core';
import { CheckToken } from '../../helpers/genarateHeader';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/User'; 
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    FormsModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  public username: String = '';
  public token: String | null = null;
  public user: User = new User();
  public me: User = new User();
  public posts: any = [];
  public followers: number = 0;
  public followings: number = 0;

  constructor(private router: Router, private route: ActivatedRoute, private userService: UserService) { }

  public ngOnInit(): void {
    if (!CheckToken()) {
      localStorage.removeItem("JWT")
      this.router.navigate(["login"])
    }
    this.token = CheckToken();
    this.username = String(this.route.snapshot.paramMap.get('username'));

    this.userService.getMe(this.token).subscribe({
      next: (res) => {
        this.me = res.me;
      },
      error: (err) => {
        console.log(err);
      }
    })
    this.LoadProfile()
  }

  public LoadProfile() {
    this.userService.getProfile(this.username, this.token).subscribe({
      next: (res) => {
        console.log(res);
        this.user = res.user;
        this.posts = res.posts;
        this.followers = res.followers
        this.followings = res.followings
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  public Home() {
    console.log("home");
    
    this.router.navigate([""])
  }
}
