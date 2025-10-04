import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { generateURL, generateHeader } from "../helpers/genarateHeader";
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PostsService {

    constructor(private http: HttpClient) { }

    public CreatePost(token: String | null, data: FormData): Observable<any> {
        return this.http.post<any>(
            generateURL("post"),
            data,
            generateHeader(token)
        )
    }

    public getAllPosts(token: String | null): Observable<any> {
        return this.http.get<any>(
            generateURL("post"),
            generateHeader(token)
        );
    }

    public getSinglePost(token: String | null, post_id: number) {
        console.log({ token });

        return this.http.get<any>(
            generateURL(`post/${post_id}`),
            generateHeader(token)
        )
    }

    public React(token: String | null, post_id: number) {
        return this.http.post<any>(
            generateURL("react"),
            {
                "post_id": post_id
            },
            generateHeader(token)
        )
    }

    public updatePost(token: String | null, post_id: number | null, data: FormData): Observable<any> {
        return this.http.put(
            generateURL(`post/${post_id}`),
            data,
            generateHeader(token)
        )
    }

    public deletePost(token: String | null, post_id: number): Observable<any> {
        return this.http.delete(
            generateURL(`post/${post_id}`),
            generateHeader(token)
        );
    }

    public CreateComment(content: String, post_id: number, token: String | null): Observable<any> {
        return this.http.post<any>(
            generateURL("comment"),
            {
                "content": content,
                "post_id": post_id
            },
            generateHeader(token)
        )
    }
}