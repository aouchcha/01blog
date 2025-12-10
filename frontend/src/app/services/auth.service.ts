import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({ 
    providedIn: 'root' 
})

export class authService {
    public constructor(private http: HttpClient) {}

    public Login(body: Object) : Observable<any> {        
        return this.http.post<any>(
            'http://localhost:8080/api/login', 
            body
        )
    }

    public Register(user: Object) : Observable<any> {
        return this.http.post(
            'http://localhost:8080/api/register', 
            user
        )
    }
}