package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Interactions.Follow.Follow;

@Repository
public interface FollowRepositry extends JpaRepository<Follow, Long> {
    boolean existsByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);

    Follow findByFollower_IdAndFollowed_Id(Long follower_id, Long followed_id);

    List<Follow> findByFollowed_Id(Long followed_id);

    Long countByFollowed_Id(Long userId);
    
    Long countByFollower_Id(Long userId);

}
