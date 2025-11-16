import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { generateURL, generateHeader } from "../helpers/genarateHeader";
import { Observable } from 'rxjs';

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

    public Report(reportted_username: String,discription: String, token: String | null): Observable<any> {
        return this.http.post<any>(
            generateURL("report"),
            {
                "reportted_username": reportted_username,
                "discription": discription
            },
            generateHeader(token)
        )
    }

    public RemoveUser(username: String, token: String | null): Observable<any> {
        console.log(token);
        
        return this.http.delete(
            generateURL(`user/${username}`),
            generateHeader(token)
        )
    }

    public BanUserr(username: String, token: String | null): Observable<any> {
        console.log({token});
        console.log("BAAAAAN");
        
        
        return this.http.put(
            generateURL(`user/${username}`),
            null,
            generateHeader(token)
        )
    }
}