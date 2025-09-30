package _blog.backend.Repos;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.UserStatsDTO;
import _blog.backend.Entitys.User.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByUsernameNotAndRoleNot(String username, Role role);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Long findIdByUsername(@Param("username") String username);

    @Query("""
                Select new _blog.backend.Entitys.User.UserStatsDTO (
                    u.id,
                    u.username,
                    u.email,
                    COUNT(DISTINCT p),
                    COUNT(DISTINCT r)
                )
                FROM User u
                LEFT JOIN u.posts p
                LEFT JOIN u.reports r
                GROUP BY u.id, u.username, u.email
            """)
    List<UserStatsDTO> findUsersStates();

}
