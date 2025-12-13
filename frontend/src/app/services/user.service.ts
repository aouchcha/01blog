import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { generateURL, generateHeader } from "../helpers/genarateHeader";
import { Observable } from 'rxjs';
import { Post } from '../models/Post';

@Injectable({
    providedIn: 'root'
})

export class UserService {
    constructor(private http: HttpClient) { }

    public ValidateToken(token: String | null): Observable<any> {
        return this.http.get(
            generateURL("validate"),
            generateHeader(token)
        )
    }

    public getMe(token: String | null): Observable<any> {
        return this.http.get<any>(
            generateURL("me"),
            generateHeader(token)
        )
    }

    public getProfile(username: String, token: String | null, lastPost: Post | null): Observable<any> {
        let params = new HttpParams();
        if (lastPost !== null) {
            params = params
                .set('lastDate', lastPost.createdAt)
                .set('lastId', lastPost.id.toString());
        }
        const options = generateHeader(token);

        const requestOptions = {
            ...options,
            params: params
        };
        return this.http.get<any>(
            generateURL(`user/${username}`),
            requestOptions
        )
    }

    public getOtherUsers(token: String | null, lastUserId: number | null): Observable<any> {
        let params = new HttpParams();
        if (lastUserId !== null) {
            params = params.set('lastUserId', lastUserId.toString());
        }
        const options = generateHeader(token);

        const requestOptions = {
            ...options,
            params: params
        };
        return this.http.get<any>(
            generateURL("users"),
            requestOptions
        )
    }

    public Follow(user_id: number, token: String | null): Observable<any> {
        return this.http.post<any>(
            generateURL("follow"),
            {
                "followed_id": user_id
            },
            generateHeader(token)
        )
    }

    public Report(reportted_username: string | null | null, discription: String, token: String | null): Observable<any> {
        return this.http.post<any>(
            generateURL("report"),
            {
                "reportted_username": reportted_username,
                "discription": discription,
                "type": "user"
            },
            generateHeader(token)
        )
    }

    
}