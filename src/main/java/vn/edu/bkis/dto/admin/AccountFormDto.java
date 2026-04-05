package vn.edu.bkis.dto.admin;

/**
 * Form data used to create a new account from the admin screen.
 */
public class AccountFormDto {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String bio;
    private String profilePictureUrl;
    private String role;

    /**
     * Create an empty form object.
     */
    public AccountFormDto() {
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
     * Get the plain-text password value.
     *
     * @return the password value
     */
    public String getPassword() {
        return password;
    }

    /**
     * Update the plain-text password value.
     *
     * @param password the password value
     */
    public void setPassword(String password) {
        this.password = password;
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
     * Get the selected role value.
     *
     * @return the selected role value
     */
    public String getRole() {
        return role;
    }

    /**
     * Update the selected role value.
     *
     * @param role the selected role value
     */
    public void setRole(String role) {
        this.role = role;
    }
}
