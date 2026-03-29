package vn.edu.bkis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.bkis.model.Course;

/**
 * Repository for Course entity providing database access for course data.
 * Handles queries for active courses, filtering, and pagination.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Retrieve top 6 active courses ordered by creation date (newest first).
     * @return list of up to 6 active courses
     */
    List<Course> findTop6ByActiveFlagTrueOrderByCreatedAtDesc();

    /**
     * Retrieve courses filtered by tag and active status.
     * @param tag the course tag/category to filter by
     * @return list of active courses with specified tag
     */
    List<Course> findTop6ByActiveFlagTrueAndTagOrderByCreatedAtDesc(String tag);

    /**
     * Retrieve related courses excluding a specific course.
     * @param id the ID of the course to exclude
     * @return list of up to 4 other active courses
     */
    List<Course> findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(Long id);

    /**
     * Retrieve top 4 active courses ordered by total students (most enrolled first).
     * @return list of up to 4 most popular active courses
     */
    List<Course> findTop4ByActiveFlagTrueOrderByTotalStudentsDesc();
}
