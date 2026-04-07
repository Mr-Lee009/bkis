package vn.edu.bkis.dto.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One student row returned by the admin students REST API.
 */
@Getter
@AllArgsConstructor
public class AdminStudentListItemDto {
    private final String id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String courseName;
    private final String status;
    private final String statusLabel;
    private final LocalDateTime joinedAt;
    private final boolean locked;
}
