package vn.edu.bkis.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.Progress;

/**
 * Repository for student progress records.
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    long countByLessonVideoId(Long lessonVideoId);

    long countByLessonVideoIdIn(Collection<Long> lessonVideoIds);
}
