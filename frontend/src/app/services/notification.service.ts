import { HttpClient } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { generateHeader, generateURL } from '../helpers/genarateHeader';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private eventSource?: EventSource;

  private notificationsSubject = new BehaviorSubject<any>(null);
  public notifications$ = this.notificationsSubject.asObservable();

  private reactionsSubject = new Subject<any>();
  public reactionsObservable = this.reactionsSubject.asObservable();

  constructor(private zone: NgZone, private http: HttpClient) { }
    


  connect(userId: number) {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      console.log("wwwwwwwwww");

      return; // already connected
    }

    this.eventSource = new EventSource(`http://localhost:8080/api/notifications/stream/${userId}`);
    console.log("cennected");

    this.eventSource.addEventListener('notification', (event: any) => {
      const notification = JSON.parse(event.data);

      // Run inside Angular zone so UI updates
      this.zone.run(() => {
        this.notificationsSubject.next(notification);
      });
    });

    this.eventSource.addEventListener('reaction', (event: any) => {
      const reaction = JSON.parse(event.data);

      // Run inside Angular zone so UI updates
      this.zone.run(() => {
        this.reactionsSubject.next(reaction);
      });
    });

    this.eventSource.onerror = (err) => {
      console.log('⚠️ SSE disconnected. Browser will auto-reconnect...', err);
    };
  }

  disconnect() {
    this.eventSource?.close();
    this.eventSource = undefined;
  }

  public getNotifs(token: String | null): Observable<any> {
    return this.http.get<any>(
      generateURL("notifications"),
      generateHeader(token)
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
