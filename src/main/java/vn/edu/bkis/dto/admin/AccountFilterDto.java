package vn.edu.bkis.dto.admin;

/**
 * Filter data used by the admin account list screen.
 */
public class AccountFilterDto {
    private String keyword;
    private String role;

    /**
     * Create an empty filter object.
     */
    public AccountFilterDto() {
    }

    /**
     * Get the free-text keyword filter.
     *
     * @return the keyword filter
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Update the free-text keyword filter.
     *
     * @param keyword the keyword filter
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Get the selected role filter.
     *
     * @return the selected role
     */
    public String getRole() {
        return role;
    }

    /**
     * Update the selected role filter.
     *
     * @param role the selected role
     */
    public void setRole(String role) {
        this.role = role;
    }
}
