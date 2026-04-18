package vn.edu.bkis.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection for the admin course detail page.
 */
public interface AdminCourseDetailProjection {
    Long getId();

    String getTitle();

    String getDescription();

    String getHighlights();

    String getTeacherId();

    String getTeacherName();

    BigDecimal getPrice();

    Integer getTotalStudents();

    Boolean getActiveFlag();

    String getTag();

    String getImageUrl();

    Integer getRating();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getEnrolledStudents();

    BigDecimal getRevenue();

    Long getModuleCount();

    Long getVideoCount();

    Long getPaymentCount();
}
