package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Post.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // List<Post> findByUser(User user);

    List<Post> findByUserId(Long userId);

    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id IN (
                    SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId
                )
                OR p.user.id = :userId
                ORDER BY p.createdAt DESC
            """)
    List<Post> findAllPostsByUserAndFollowedUsers(@Param("userId") Long userId);

    @SuppressWarnings("null")
    boolean existsById(Long id);

    // List<Post> findAllByUser_Id(Long userId);
}
