package vn.edu.bkis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for detailed course information displayed on course detail page.
 * Contains course metadata, instructor info, lessons, pricing, and related courses.
 */
public record CourseDetailPageDto(
        Long id,
        String title,
        String description,
        String instructorName,
        String instructorBio,
        String profilePictureUrl,
        String instructorEmail,
        String imageUrl,
        String tag,
        BigDecimal price,
        Integer totalStudents,
        Integer rating,
        Integer totalReviews,
        Integer lessonCount,
        Integer durationHours,
        List<String> highlights,
        List<CourseLessonDto> lessons,
        List<HomeCourseDto> relatedCourses
) {
}
