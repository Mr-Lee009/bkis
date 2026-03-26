package vn.edu.bkis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.CourseReview;

/**
 * Repository for CourseReview entity providing database access operations.
 * Handles queries for course ratings, reviews, and student feedback.
 */
@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    /**
     * Count total reviews for a specific course.
     * @param courseId the ID of the course
     * @return total number of reviews
     */
    long countByCourseId(Long courseId);

    /**
     * Calculate average rating score for a course from all reviews.
     * @param courseId the ID of the course
     * @return average rating (0 if no reviews exist)
     */
    @Query("select coalesce(avg(cr.rating), 0) from CourseReview cr where cr.courseId = :courseId")
    Double findAverageRatingByCourseId(Long courseId);

    /**
     * Retrieve the 3 most recent reviews for a course.
     * @param courseId the ID of the course
     * @return list of up to 3 most recent reviews
     */
    List<CourseReview> findTop3ByCourseIdOrderByCreatedAtDesc(Long courseId);
}