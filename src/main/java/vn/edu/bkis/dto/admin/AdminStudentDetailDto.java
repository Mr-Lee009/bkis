package vn.edu.bkis.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Detailed profile data shown in the admin student modal.
 */
@Getter
@AllArgsConstructor
public class AdminStudentDetailDto {
    private final String id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String profilePictureUrl;
    private final String bio;
    private final boolean locked;
    private final LocalDateTime joinedAt;
    private final Long courseId;
    private final String courseName;
    private final String status;
    private final String statusLabel;
    private final LocalDateTime enrolledAt;
    private final LocalDateTime expiresAt;
    private final Integer progressPercent;
    private final Long completedVideos;
    private final Long totalVideos;
    private final String progressLabel;
    private final LocalDateTime lastActivityAt;
    private final String cohortCode;
    private final String mentorId;
    private final List<String> goals;
    private final String note;
}
