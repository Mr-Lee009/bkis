package vn.edu.bkis.dto.admin.student;

import java.time.LocalDateTime;

/**
 * Projection used to load one admin student detail view.
 */
public interface AdminStudentDetailProjection {
    String getId();

    String getUsername();

    String getFullName();

    String getEmail();

    String getProfilePictureUrl();

    String getBio();

    Boolean getLocked();

    LocalDateTime getJoinedAt();

    Long getCourseId();

    String getCourseName();

    String getEnrollmentStatus();

    LocalDateTime getEnrolledAt();

    LocalDateTime getExpiresAt();

    Long getCompletedVideos();

    Long getTotalVideos();

    LocalDateTime getLastActivityAt();
}
