package vn.edu.bkis.dto;

import java.math.BigDecimal;
import java.util.List;
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
    List<CoursePaymentGatewayDto> paymentGateways;
}
