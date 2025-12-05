// src/app/ViewModel/not-found/not-found.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-not-found',
  standalone: true,
  template: `
    <div class="not-found-container">
      <h1>404 - Page Not Found</h1>
      <p>Redirecting to home...</p>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100vh;
      text-align: center;
    }
  `]
})
export class NotFound implements OnInit {
  private toast = inject(ToastService);
  private router = inject(Router);

  ngOnInit() {
    this.toast.showError('Page not found. Redirecting to home.');
    setTimeout(() => {
      this.router.navigate(['']);
    }, 2000); 
  }
}