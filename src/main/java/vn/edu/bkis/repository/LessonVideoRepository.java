package vn.edu.bkis.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.LessonVideo;

/**
 * Repository for LessonVideo entity.
 * Provides access to videos grouped by lesson.
 */
@Repository
public interface LessonVideoRepository extends JpaRepository<LessonVideo, Long> {

    /**
     * Retrieve all videos of a lesson ordered by position.
     * @param lessonId the lesson identifier
     * @return ordered lesson videos
     */
    List<LessonVideo> findByLessonIdOrderByPositionAsc(Long lessonId);

    List<LessonVideo> findByLessonIdInOrderByPositionAsc(Collection<Long> lessonIds);
}
