package vn.edu.bkis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT COUNT(u.id) FROM users u WHERE u.role = 'STUDENT' ",nativeQuery = true)
    Long countAllStudents();

    @Query(value = "SELECT COUNT(u.id) FROM users u WHERE u.role = 'STUDENT' " +
                   "AND (u.created_at BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE()))",nativeQuery = true)
    Long countAllStudentsCreateThisMonth();

}
