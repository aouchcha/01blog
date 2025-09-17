package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // List<Post> findByUser(User user);
    // List<Post> findByUserId(Long userId);
}
