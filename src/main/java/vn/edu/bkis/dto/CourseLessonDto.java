package vn.edu.bkis.dto;

import java.util.List;

/**
 * Data Transfer Object for course lesson information in curriculum.
 * Represents individual lessons within a course structure.
 */
public record CourseLessonDto(
        Long id,
        Integer position,
        String title,
        String description,
        Integer durationMinutes,
        List<CourseLessonVideoDto> videos
) {
}
