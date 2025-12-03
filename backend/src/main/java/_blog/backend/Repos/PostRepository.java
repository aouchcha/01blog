package _blog.backend.Repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Post.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserIdOrderByIdDesc(Long userId);

    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id = :userId
                AND (:isAdmin = true OR p.isHidden = false)
                AND (p.createdAt < :lastDate OR (p.createdAt = :lastDate AND p.id < :lastId))
                ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findNextPosts(@Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            @Param("lastDate") LocalDateTime lastDate,
            @Param("lastId") Long lastId,
            Pageable pageable);

    @Query("""
                SELECT p FROM Post p
                WHERE p.user.id = :userId
                AND (:isAdmin = true OR p.isHidden = false)
                ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findTop10ByUserIdOrderByCreatedAtDescIdDesc(@Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin,
            Pageable pageable);

    @Query("""
                SELECT p FROM Post p
                WHERE p.isHidden = false
                AND (
                    p.user.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId)
                    OR p.user.id = :userId
                )
                ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findInitialFeedPosts(@Param("userId") Long userId, Pageable pageable);

    @Query("""
                SELECT p FROM Post p
                WHERE p.isHidden = false
                AND (
                    p.user.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId)
                    OR p.user.id = :userId
                )
                AND (
                    p.createdAt < :lastDate
                    OR (p.createdAt = :lastDate AND p.id < :lastId)
                )
                ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findNextFeedPosts(@Param("userId") Long userId,
            @Param("lastDate") LocalDateTime lastDate,
            @Param("lastId") Long lastId,
            Pageable pageable);

    // @SuppressWarnings("null")
    boolean existsById(Long id);

    Long countByUserId(Long id);

}
