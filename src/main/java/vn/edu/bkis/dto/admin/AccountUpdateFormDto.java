package vn.edu.bkis.dto.admin;

/**
 * Form data used to update an existing account from the modal dialog.
 */
public class AccountUpdateFormDto {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    private String profilePictureUrl;
    private Boolean locked;

    /**
     * Create an empty update form object.
     */
    public AccountUpdateFormDto() {
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
     * Update the account identifier.
     *
     * @param id the account identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the username value.
     *
     * @return the username value
     */
    public String getUsername() {
        return username;
    }

    /**
     * Update the username value.
     *
     * @param username the username value
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the full name value.
     *
     * @return the full name value
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Update the full name value.
     *
     * @param fullName the full name value
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Get the email value.
     *
     * @return the email value
     */
    public String getEmail() {
        return email;
    }

    /**
     * Update the email value.
     *
     * @param email the email value
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the role value.
     *
     * @return the role value
     */
    public String getRole() {
        return role;
    }

    /**
     * Update the role value.
     *
     * @param role the role value
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Get the biography value.
     *
     * @return the biography value
     */
    public String getBio() {
        return bio;
    }

    /**
     * Update the biography value.
     *
     * @param bio the biography value
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Get the profile picture URL value.
     *
     * @return the profile picture URL value
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Update the profile picture URL value.
     *
     * @param profilePictureUrl the profile picture URL value
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Check whether the account should be locked.
     *
     * @return true when the account should be locked
     */
    public Boolean getLocked() {
        return locked;
    }

    /**
     * Update the lock flag.
     *
     * @param locked the lock flag
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}
