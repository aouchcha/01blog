// import { Injectable } from '@angular/core';
// import { BehaviorSubject } from 'rxjs';

// @Injectable({
//   providedIn: 'root'
// })
// export class StateService {
//   // Post state
//   private selectedPostSource = new BehaviorSubject<number>(0);
//   selectedPostId$ = this.selectedPostSource.asObservable();

//   // User state
//   private UserSource = new BehaviorSubject<number>(0);
//   SelectedUserId$ = this.UserSource.asObservable();

//   private CurrentUserSource = new BehaviorSubject<number>(0);
//   CurrentUsser$ = this.CurrentUserSource.asObservable();

//   // ---- Post methods ----
//   setPostId(post_id: number) {
//     this.selectedPostSource.next(post_id);
//   }

//   // ---- User methods ----
//   setUserId(user_id: number) {
//     this.UserSource.next(user_id);
//   }

//   setCurrentUser(me: any) {
//     this.CurrentUserSource.next(me);
//   }
// }
