import { Observable } from "rxjs";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { generateHeader, generateURL } from "../helpers/genarateHeader";

@Injectable({ 
    providedIn: 'root' 
})

export class AdminService {
    public constructor(private http: HttpClient) {}

    public getDashBoard(token: String | null, lastUserId: number | null): Observable<any> {
         let params = new HttpParams();
        if (lastUserId !== null) {
            params = params.set('lastUserId', lastUserId.toString());
        }
        const options = generateHeader(token);

        const requestOptions = {
            ...options,
            params: params
        };

        return this.http.get(
            generateURL("admin"),
            requestOptions
        )
    }

    public loadReports(token: String | null, lastReport: any) : Observable<any> {
        let params = new HttpParams();
        if (lastReport !== null) {
            params = params
                .set('lastDate', lastReport.createdAt)
                .set('lastId', lastReport.id.toString());
        }
        const options = generateHeader(token);

        const requestOptions = {
            ...options,
            params: params
        };

        return this.http.get(
            generateURL("admin/reports"),
            requestOptions
        )
    }

    public RemoveReport(token: String|null, report_id: number|null) : Observable<any> {
        return this.http.delete(
            generateURL(`admin/reports/${report_id}`),
            generateHeader(token)
        )
    }

    public RemoveUser(username: string | null, token: String | null): Observable<any> {
        return this.http.delete(
            generateURL(`admin/${username}`),
            generateHeader(token)
        )
    }

    public BanUserr(username: string | null, token: String | null): Observable<any> {
        return this.http.put(
            generateURL(`admin/ban/${username}`),
            null,
            generateHeader(token)
        )
    }

    public UnBanUserr(username: string | null, token: String | null): Observable<any> {
        return this.http.put(
            generateURL(`admin/unban/${username}`),
            null,
            generateHeader(token)
        )
    }
}