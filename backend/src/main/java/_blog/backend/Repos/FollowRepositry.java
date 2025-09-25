package _blog.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Interactions.Follow.Follow;

@Repository
public interface FollowRepositry extends JpaRepository<Follow, Long> {
    boolean existsByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);

    Follow findByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);

    Long countByFollowed_Id(Long userId); // counts followers

    Long countByFollower_Id(Long userId); // counts followings

}
