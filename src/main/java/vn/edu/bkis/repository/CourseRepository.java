package vn.edu.bkis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.bkis.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findTop6ByActiveFlagTrueOrderByCreatedAtDesc();

    List<Course> findTop6ByActiveFlagTrueAndTagOrderByCreatedAtDesc(String tag);

    List<Course> findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(Long id);
}
