package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.bkis.dto.admin.student.AdminStudentDetailProjection;
import vn.edu.bkis.dto.admin.student.AdminStudentListProjection;
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
     * Check whether a username already exists for local or SSO-created accounts.
     *
     * @param username the username candidate to validate
     * @return true when the username is already taken
     */
    boolean existsByUsername(String username);

    /**
     * Find a user by email to prevent duplicate account creation.
     *
     * @param email the email address to search for
     * @return the matching user if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users that belong to the provided roles ordered by name.
     *
     * @param roles roles to include
     * @return ordered user list
     */
    List<User> findByRoleInOrderByFullNameAsc(List<UserRole> roles);

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

    /**
     * Search students for the admin students REST API with pagination.
     *
     * The query joins the latest enrollment, when present, so the table can display
     * the most recent course and enrollment status without loading full aggregates.
     *
     * @param keyword the free-text keyword matching username, full name, or email
     * @param pageable the requested page config
     * @return paged student rows
     */
    @Query(value = """
        SELECT
            u.id AS id,
            u.username AS username,
            u.full_name AS fullName,
            u.email AS email,
            c.title AS courseName,
            COALESCE(progress_stats.completed_videos, 0) AS completedVideos,
            COALESCE(video_stats.total_videos, 0) AS totalVideos,
            en.status AS enrollmentStatus,
            u.created_at AS joinedAt,
            u.locked AS locked
        FROM users u
        LEFT JOIN enrollments en ON en.id = (
            SELECT e2.id
            FROM enrollments e2
            WHERE e2.student_id = u.id
            ORDER BY e2.enrolled_at DESC, e2.id DESC
            LIMIT 1
        )
        LEFT JOIN courses c ON c.id = en.course_id
        LEFT JOIN (
            SELECT l.course_id AS course_id, COUNT(lv.id) AS total_videos
            FROM lessons l
            JOIN lesson_videos lv ON lv.lesson_id = l.id
            GROUP BY l.course_id
        ) video_stats ON video_stats.course_id = c.id
        LEFT JOIN (
            SELECT l.course_id AS course_id, p.student_id AS student_id, COUNT(DISTINCT p.lesson_video_id) AS completed_videos
            FROM progress p
            JOIN lesson_videos lv ON lv.id = p.lesson_video_id
            JOIN lessons l ON l.id = lv.lesson_id
            WHERE p.is_completed = TRUE
            GROUP BY l.course_id, p.student_id
        ) progress_stats ON progress_stats.course_id = c.id AND progress_stats.student_id = u.id
        WHERE u.role = 'STUDENT'
          AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (
            :status IS NULL OR :status = ''
            OR (:status = 'ONBOARDING' AND en.id IS NULL)
            OR (:status <> 'ONBOARDING' AND en.status = :status)
          )
        ORDER BY u.created_at DESC, u.id DESC
        """,
        countQuery = """
        SELECT COUNT(u.id)
        FROM users u
        LEFT JOIN enrollments en ON en.id = (
            SELECT e2.id
            FROM enrollments e2
            WHERE e2.student_id = u.id
            ORDER BY e2.enrolled_at DESC, e2.id DESC
            LIMIT 1
        )
        WHERE u.role = 'STUDENT'
          AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (
            :status IS NULL OR :status = ''
            OR (:status = 'ONBOARDING' AND en.id IS NULL)
            OR (:status <> 'ONBOARDING' AND en.status = :status)
          )
        """,
        nativeQuery = true)
    Page<AdminStudentListProjection> searchAdminStudents(@Param("keyword") String keyword,
                                                         @Param("status") String status,
                                                         Pageable pageable);

    /**
     * Load one student profile for the admin detail modal.
     *
     * @param studentId requested student id
     * @return detail projection when the student exists
     */
    @Query(value = """
        SELECT
            u.id AS id,
            u.username AS username,
            u.full_name AS fullName,
            u.email AS email,
            u.profile_picture_url AS profilePictureUrl,
            u.bio AS bio,
            u.locked AS locked,
            u.created_at AS joinedAt,
            c.id AS courseId,
            c.title AS courseName,
            en.status AS enrollmentStatus,
            en.enrolled_at AS enrolledAt,
            en.expires_at AS expiresAt,
            COALESCE(progress_stats.completed_videos, 0) AS completedVideos,
            COALESCE(video_stats.total_videos, 0) AS totalVideos,
            progress_stats.last_activity_at AS lastActivityAt
        FROM users u
        LEFT JOIN enrollments en ON en.id = (
            SELECT e2.id
            FROM enrollments e2
            WHERE e2.student_id = u.id
            ORDER BY e2.enrolled_at DESC, e2.id DESC
            LIMIT 1
        )
        LEFT JOIN courses c ON c.id = en.course_id
        LEFT JOIN (
            SELECT l.course_id AS course_id, COUNT(lv.id) AS total_videos
            FROM lessons l
            JOIN lesson_videos lv ON lv.lesson_id = l.id
            GROUP BY l.course_id
        ) video_stats ON video_stats.course_id = c.id
        LEFT JOIN (
            SELECT
                l.course_id AS course_id,
                p.student_id AS student_id,
                COUNT(DISTINCT CASE WHEN p.is_completed = TRUE THEN p.lesson_video_id END) AS completed_videos,
                MAX(p.updated_at) AS last_activity_at
            FROM progress p
            JOIN lesson_videos lv ON lv.id = p.lesson_video_id
            JOIN lessons l ON l.id = lv.lesson_id
            GROUP BY l.course_id, p.student_id
        ) progress_stats ON progress_stats.course_id = c.id AND progress_stats.student_id = u.id
        WHERE u.role = 'STUDENT'
          AND u.id = :studentId
        """, nativeQuery = true)
    Optional<AdminStudentDetailProjection> findAdminStudentDetailById(@Param("studentId") String studentId);

    /**
     * Count students whose latest enrollment is active.
     *
     * @return total active students
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM users u
        LEFT JOIN enrollments en ON en.id = (
            SELECT e2.id
            FROM enrollments e2
            WHERE e2.student_id = u.id
            ORDER BY e2.enrolled_at DESC, e2.id DESC
            LIMIT 1
        )
        WHERE u.role = 'STUDENT'
          AND en.status = 'ACTIVE'
        """, nativeQuery = true)
    long countActiveStudentsForAdmin();

    /**
     * Count students who have not been enrolled yet and are treated as onboarding.
     *
     * @return total onboarding students
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM users u
        LEFT JOIN enrollments en ON en.id = (
            SELECT e2.id
            FROM enrollments e2
            WHERE e2.student_id = u.id
            ORDER BY e2.enrolled_at DESC, e2.id DESC
            LIMIT 1
        )
        WHERE u.role = 'STUDENT'
          AND en.id IS NULL
        """, nativeQuery = true)
    long countOnboardingStudentsForAdmin();

}
