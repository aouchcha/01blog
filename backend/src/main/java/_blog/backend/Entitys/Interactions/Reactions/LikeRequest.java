package _blog.backend.Entitys.Interactions.Reactions;

public class LikeRequest {
    private Long post_id;

    public void setPost_id(Long post_id) {
        this.post_id = post_id;
    }

    public Long getPost_id() {
        return post_id;
    }
}
