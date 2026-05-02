package vn.edu.bkis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class MyCourseDto {
    Long courseId;
    String title;
    String description;
    String imageUrl;
    BigDecimal price;
    String enrollmentStatus;
    LocalDateTime enrolledAt;
}
