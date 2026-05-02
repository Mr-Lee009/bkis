package vn.edu.bkis.dto.admin.student;

import java.time.LocalDateTime;

/**
 * Projection used by the admin student list query.
 */
public interface AdminStudentListProjection {
    String getId();

    String getUsername();

    String getFullName();

    String getEmail();

    String getCourseName();

    Long getCompletedVideos();

    Long getTotalVideos();

    String getEnrollmentStatus();

    LocalDateTime getJoinedAt();

    Boolean getLocked();
}
