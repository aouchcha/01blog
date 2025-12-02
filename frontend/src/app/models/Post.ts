import { User } from "./User"

export class Post {
    createdAt: string = "";
    title: string = "";
    description: string = "";
    id: number = 0;
    likeCount: number = 0;
    commentsCount: number = 0;
    isHidden: boolean = false;
    media: File | null = null;
    mediaUrl: string = ""
    user: User = new User();
}