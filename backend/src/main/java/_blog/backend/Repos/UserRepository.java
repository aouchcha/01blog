package _blog.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
