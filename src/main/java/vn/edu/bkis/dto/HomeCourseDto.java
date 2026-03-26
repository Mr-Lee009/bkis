package vn.edu.bkis.dto;

import java.math.BigDecimal;

public record HomeCourseDto(Long id, String title, String description, String instructorName,
                            BigDecimal price, Integer totalStudents, Integer durationHours,
                            Integer rating, Integer reviewCount, String imageUrl, String tag) {
}
