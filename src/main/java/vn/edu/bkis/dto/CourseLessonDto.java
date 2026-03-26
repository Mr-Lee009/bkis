package vn.edu.bkis.dto;

/**
 * Data Transfer Object for course lesson information in curriculum.
 * Represents individual lessons within a course structure.
 */
public record CourseLessonDto(
        Long id,
        Integer position,
        String title,
        String description
) {
}