import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StateService {
  // Post state
  private selectedPostSource = new BehaviorSubject<Number>(0);
  selectedPostId$ = this.selectedPostSource.asObservable();

  // User state
  private UserSource = new BehaviorSubject<Number>(0);
  SelectedUserId$ = this.UserSource.asObservable();

  private CurrentUserSource = new BehaviorSubject<Number>(0);
  CurrentUsser$ = this.CurrentUserSource.asObservable();

  // ---- Post methods ----
  setPostId(post_id: Number) {
    this.selectedPostSource.next(post_id);
  }

  // ---- User methods ----
  setUserId(user_id: Number) {
    this.UserSource.next(user_id);
  }

  setCurrentUser(me: any) {
    this.CurrentUserSource.next(me);
  }
}
