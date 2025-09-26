import { User } from "./User"

export class Post {
    createdAt: string = "";
    description: String = "";
    id: number = 0;
    likeCount: Number = 0;
    commentsCount: Number = 0;
    media: String = "";
    mediaUrl: String = ""
    user: User = new User();
}