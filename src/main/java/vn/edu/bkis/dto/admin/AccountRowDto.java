package vn.edu.bkis.dto.admin;

/**
 * Row data rendered in the admin account table.
 */
public class AccountRowDto {
    private final String id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;
    private final boolean locked;
    private final int failedLoginAttempts;
    private final String createdAt;

    /**
     * Create a table row DTO for an account item.
     *
     * @param id the account identifier
     * @param username the username
     * @param fullName the full name
     * @param email the email
     * @param role the role label
     * @param locked the lock status
     * @param failedLoginAttempts the number of failed logins
     * @param createdAt the formatted created-at value
     */
    public AccountRowDto(String id, String username, String fullName, String email, String role,
                         boolean locked, int failedLoginAttempts, String createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.locked = locked;
        this.failedLoginAttempts = failedLoginAttempts;
        this.createdAt = createdAt;
    }

    /**
     * Get the account identifier.
     *
     * @return the account identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Get the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the role label.
     *
     * @return the role label
     */
    public String getRole() {
        return role;
    }

    /**
     * Check whether the account is locked.
     *
     * @return true when the account is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Get the number of failed login attempts.
     *
     * @return the failed login attempts
     */
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    /**
     * Get the formatted created-at label.
     *
     * @return the formatted created-at label
     */
    public String getCreatedAt() {
        return createdAt;
    }
}
