package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.Lesson;

/**
 * Repository for Lesson entity managing course curriculum data.
 * Provides database operations for accessing lesson information.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Retrieve all lessons for a course ordered by position.
     * @param courseId the ID of the course
     * @return list of lessons sorted by position ascending
     */
    List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);

    /**
     * Count total lessons in a course.
     * @param courseId the ID of the course
     * @return number of lessons
     */
    long countByCourseId(Long courseId);

    Optional<Lesson> findByIdAndCourseId(Long id, Long courseId);
}
