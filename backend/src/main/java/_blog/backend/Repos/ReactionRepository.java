package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Interactions.Reactions.Like;

@Repository
public interface ReactionRepository extends JpaRepository<Like, Long>{
    Like findByPost_IdAndUser_Username(Long post_id, String username);
    List<Like> findAllByPost_IdAndUser_Username(Long post_id, String username);
    boolean existsByPost_IdAndUser_Username(Long postId, String username);
    Long countByPost_id(Long post_id);
}
