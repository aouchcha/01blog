import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, shareReplay, Subject } from 'rxjs';
import { generateHeader, generateURL } from '../helpers/genarateHeader';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private eventSource?: EventSource;

  private notificationsSubject = new BehaviorSubject<any>(null);
  public notifications$ = this.notificationsSubject.asObservable().pipe(shareReplay(1));

  private reactionsSubject = new Subject<any>();
  public reactionsObservable = this.reactionsSubject.asObservable().pipe(shareReplay(1));

  constructor(private zone: NgZone, private http: HttpClient) {}



  connect(userId: number) {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {

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
