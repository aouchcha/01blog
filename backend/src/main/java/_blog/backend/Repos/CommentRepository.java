package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import _blog.backend.Entitys.Comment.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Long countByPost_id(Long post_id);
    List<Comment> findAllByPost_id(Long post_id);
}
