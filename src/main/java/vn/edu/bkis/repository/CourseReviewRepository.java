package vn.edu.bkis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.CourseReview;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    long countByCourseId(Long courseId);

    @Query("select coalesce(avg(cr.rating), 0) from CourseReview cr where cr.courseId = :courseId")
    Double findAverageRatingByCourseId(Long courseId);

    List<CourseReview> findTop3ByCourseIdOrderByCreatedAtDesc(Long courseId);
}