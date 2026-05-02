package vn.edu.bkis.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CourseSignupPageDto {
    Long courseId;
    String courseTitle;
    String courseDescription;
    String courseImageUrl;
    BigDecimal price;
    String studentFullName;
    String studentEmail;
    boolean alreadyEnrolled;
}
