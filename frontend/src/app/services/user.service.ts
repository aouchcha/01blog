import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { generateURL, generateHeader } from "../helpers/genarateHeader";
import { Observable } from 'rxjs';
import { User } from '../models/User';

@Injectable({
    providedIn: 'root'
})

export class UserService {
    constructor(private http: HttpClient) { }

    public getMe(token: String | null): Observable<any> {
        return this.http.get<any>(
            generateURL("me"),
            generateHeader(token)
        )
    }

    public getProfile(username: String, token: String | null): Observable<any> {
        return this.http.get<any>(
            generateURL(`user/${username}`),
            generateHeader(token)
        )
    }

    public getOtherUsers(token: String | null): Observable<any> {
        return this.http.get<any>(
            generateURL("users"),
            generateHeader(token)
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
}