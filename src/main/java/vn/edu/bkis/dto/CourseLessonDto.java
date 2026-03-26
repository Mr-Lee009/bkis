package vn.edu.bkis.dto;

public record CourseLessonDto(
        Long id,
        Integer position,
        String title,
        String description
) {
}