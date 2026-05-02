package vn.edu.bkis.dto.admin.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection for one course row in the admin list.
 */
public interface AdminCourseListProjection {
    Long getId();

    String getTitle();

    String getTag();

    BigDecimal getPrice();

    Boolean getActiveFlag();

    String getCourseStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getTeacherName();

    Long getEnrolledStudents();

    BigDecimal getRevenue();

    Long getModuleCount();

    Long getVideoCount();
}
