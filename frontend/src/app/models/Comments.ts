import { User } from "./User"
import { Post } from "./Post";

export class Comment {
    id: number = 0;
    content: String = '';
    createdAt: string = '';
    user: User = new User();
    post: Post = new Post();
}