package _blog.backend.Entitys.Interactions.Follow;

public class FollowRequest {
    private Long followed_id;
    private Long follower_id;

    public void setFollowed_id(Long followed_id) {
        this.followed_id = followed_id;
    }

    public Long getFollowed_id() {
        return followed_id;
    }

    public void setFollower_id(Long follower_id) {
        this.follower_id = follower_id;
    }

    public Long getFollower_id() {
        return follower_id;
    }
}
