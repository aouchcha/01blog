import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { generateURL, generateHeader } from "../helpers/genarateHeader";
import { Observable } from 'rxjs';
import { Post } from '../models/Post';

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

    public getAllPosts(token: String | null, lastPost: Post | null | undefined): Observable<any> {
        let params = new HttpParams();
        if (lastPost !== null && lastPost != undefined) {
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
            generateURL("post"),
            requestOptions
        );
    }

    public getSinglePost(token: String | null, post_id: number) {
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

    public updatePost(token: String | null, post_id: number | null, data: FormData, removed: boolean): Observable<any> {
        let params = new HttpParams().set("removed", removed);
        const options = generateHeader(token);

        const requestOptions = {
            ...options,
            params: params
        };
        return this.http.put(
            generateURL(`post/${post_id}`),
            data,
            requestOptions
        )
    }

    public deletePost(token: String | null, post_id: number | null): Observable<any> {
        return this.http.delete(
            generateURL(`post/${post_id}`),
            generateHeader(token)
        );
    }

    public ReportPost(token: String | null, post_id: number | null, description: String): Observable<any> {
        return this.http.post<any>(
            generateURL("report"),
            {
                "post_id": post_id,
                "discription": description,
                "type": "post"
            },
            generateHeader(token)
        )
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

    public DeleteComment(token: String | null, comment_id: number | null): Observable<any> {
        return this.http.delete(
            generateURL(`comment/${comment_id}`),
            generateHeader(token)
        );
    }

    public HidePost(token: String | null, post_id: number | null): Observable<any> {
        return this.http.patch(
            generateURL(`post/hide/${post_id}`),
            {},
            generateHeader(token)
        )
    }

    public UnhidePost(token: String | null, post_id: number | null): Observable<any> {
        return this.http.patch(
            generateURL(`post/unhide/${post_id}`),
            {},
            generateHeader(token)
        )
    }
}