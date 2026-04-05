package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    /**
     * Find a user by username for authentication and admin checks.
     *
     * @param username the username to search for
     * @return the matching user if present
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email to prevent duplicate account creation.
     *
     * @param email the email address to search for
     * @return the matching user if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Count all accounts that belong to a specific role.
     *
     * @param role the role to count
     * @return total accounts with the given role
     */
    long countByRole(UserRole role);

    /**
     * Count all unlocked accounts.
     *
     * @return total unlocked accounts
     */
    long countByLockedFalse();

    /**
     * Count all locked accounts.
     *
     * @return total locked accounts
     */
    long countByLockedTrue();

    /**
     * Search accounts by keyword for the admin management page.
     *
     * @param keyword the free-text keyword that matches username, full name, or email
     * @return ordered account list for rendering
     */
    @Query("""
        SELECT u
        FROM User u
        WHERE (:keyword IS NULL OR :keyword = '' OR lower(u.username) LIKE lower(concat('%', :keyword, '%'))
            OR lower(u.fullName) LIKE lower(concat('%', :keyword, '%'))
            OR lower(u.email) LIKE lower(concat('%', :keyword, '%')))
        ORDER BY u.createdAt DESC, u.username ASC
        """)
    List<User> searchAccounts(@Param("keyword") String keyword);

    /**
     * Count all students for the admin dashboard.
     *
     * @return total student accounts
     */
    @Query(value = "SELECT COUNT(u.id) FROM users u WHERE u.role = 'STUDENT' ",nativeQuery = true)
    Long countAllStudents();

    /**
     * Count student accounts created in the current month for the admin dashboard.
     *
     * @return total students created this month
     */
    @Query(value = "SELECT COUNT(u.id) FROM users u WHERE u.role = 'STUDENT' " +
                   "AND (u.created_at BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE()))",nativeQuery = true)
    Long countAllStudentsCreateThisMonth();

}
