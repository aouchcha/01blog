package _blog.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;

import _blog.backend.Entitys.Interactions.Follow.Follow;

public interface FollowRepositry extends JpaRepository<Follow, Long>{
    boolean existsByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);
    Follow findByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);
}
