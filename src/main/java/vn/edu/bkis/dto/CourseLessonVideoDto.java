package vn.edu.bkis.dto;

/**
 * Data Transfer Object for lesson video information.
 * Represents a video item within a lesson.
 */
public record CourseLessonVideoDto(
        Long id,
        Long lessonId,
        String title,
        String videoUrl,
        Integer durationMinutes,
        Integer position,
        boolean locked
) {
}
