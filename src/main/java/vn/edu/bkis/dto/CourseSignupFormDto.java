package vn.edu.bkis.dto;

import lombok.Data;

@Data
public class CourseSignupFormDto {
    private String phone;
    private String learningGoal;
    private String learningMode;
    private String couponCode;
    private String paymentMethod;
    private boolean acceptedTerms;
}
