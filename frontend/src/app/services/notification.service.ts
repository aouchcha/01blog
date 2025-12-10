import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, shareReplay, Subject } from 'rxjs';
import { generateHeader, generateURL } from '../helpers/genarateHeader';
import { ToastService } from './toast.service';
import { Router } from '@angular/router';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private eventSource?: EventSource;

  private notificationsSubject = new BehaviorSubject<any>(null);
  public notifications$ = this.notificationsSubject.asObservable().pipe(shareReplay(1));

  private reactionsSubject = new Subject<any>();
  public reactionsObservable = this.reactionsSubject.asObservable().pipe(shareReplay(1));

  constructor(private zone: NgZone, private http: HttpClient, private toast: ToastService, private router: Router, private userService: UserService) { }



  connect(userId: number, token: String | null) {
   this.userService.ValidateToken(token).subscribe({
      next: () => {
        this.EstablishConnection(userId, token)
      },
    })


  }

  public EstablishConnection(userId: number, token: String | null) {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      return; // already connected
    }

    this.eventSource = new EventSource(`http://localhost:8080/api/notifications/stream/${userId}?token=${token}`);

    this.eventSource.addEventListener('notification', (event: any) => {
      const notification = JSON.parse(event.data);

      this.zone.run(() => {
        this.notificationsSubject.next(notification);
      });
    });

    this.eventSource.addEventListener('reaction', (event: any) => {
      const reaction = JSON.parse(event.data);

      this.zone.run(() => {
        this.reactionsSubject.next(reaction);
      });
    });

    this.eventSource.onerror = (err: any) => {
      this.disconnect();
      this.toast.showError("Error With SSE try later");
    };
  }

  disconnect() {
    this.eventSource?.close();
    this.eventSource = undefined;
  }

  public getNotifs(token: String | null, lastNotif: any): Observable<any> {
    let params = new HttpParams();
    if (lastNotif !== null) {
      params = params
        .set('lastDate', lastNotif.createdAt)
        .set('lastId', lastNotif.id.toString());
    }
    const options = generateHeader(token);

    const requestOptions = {
      ...options,
      params: params
    };
    return this.http.get<any>(
      generateURL("notifications"),
      requestOptions
    );
  }

  public markAsRead(token: String | null, notification_id: number): Observable<any> {
    return this.http.put<any>(
      generateURL(`notifications/${notification_id}`),
      {},
      generateHeader(token)
    );
  }
}
