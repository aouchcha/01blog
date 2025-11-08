import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { generateHeader, generateURL } from "../helpers/genarateHeader";

@Injectable({ 
    providedIn: 'root' 
})

export class AdminService {
    public constructor(private http: HttpClient) {}

    public getDashBoard(token: String | null): Observable<any> {
        return this.http.get(
            generateURL("admin"),
            generateHeader(token)
        )
    }

    public loadReports(token: String | null) : Observable<any> {
        // console.log({token});
        
        return this.http.get(
            generateURL("admin/reports"),
            generateHeader(token)
        )
    }
}