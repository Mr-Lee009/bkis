package vn.edu.bkis.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Row data rendered in the admin account table.
 */
@Getter
@AllArgsConstructor
public class AccountRowDto {
    private final long rowNumber;
    private final String id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;
    private final String bio;
    private final String profilePictureUrl;
    private final boolean locked;
    private final int failedLoginAttempts;
    private final String createdAt;
}
