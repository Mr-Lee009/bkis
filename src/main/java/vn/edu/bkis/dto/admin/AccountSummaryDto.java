package vn.edu.bkis.dto.admin;

/**
 * Summary metrics rendered at the top of the admin account page.
 */
public class AccountSummaryDto {
    private final long totalAccounts;
    private final long adminAccounts;
    private final long teacherAccounts;
    private final long studentAccounts;
    private final long activeAccounts;
    private final long lockedAccounts;

    /**
     * Create the page summary DTO.
     *
     * @param totalAccounts total number of accounts
     * @param adminAccounts total number of admin accounts
     * @param teacherAccounts total number of teacher and instructor accounts
     * @param studentAccounts total number of student accounts
     * @param activeAccounts total number of unlocked accounts
     * @param lockedAccounts total number of locked accounts
     */
    public AccountSummaryDto(long totalAccounts, long adminAccounts, long teacherAccounts,
                             long studentAccounts, long activeAccounts, long lockedAccounts) {
        this.totalAccounts = totalAccounts;
        this.adminAccounts = adminAccounts;
        this.teacherAccounts = teacherAccounts;
        this.studentAccounts = studentAccounts;
        this.activeAccounts = activeAccounts;
        this.lockedAccounts = lockedAccounts;
    }

    /**
     * Get total number of accounts.
     *
     * @return total number of accounts
     */
    public long getTotalAccounts() {
        return totalAccounts;
    }

    /**
     * Get total number of admin accounts.
     *
     * @return total number of admin accounts
     */
    public long getAdminAccounts() {
        return adminAccounts;
    }

    /**
     * Get total number of teacher and instructor accounts.
     *
     * @return total number of teacher and instructor accounts
     */
    public long getTeacherAccounts() {
        return teacherAccounts;
    }

    /**
     * Get total number of student accounts.
     *
     * @return total number of student accounts
     */
    public long getStudentAccounts() {
        return studentAccounts;
    }

    /**
     * Get total number of unlocked accounts.
     *
     * @return total number of unlocked accounts
     */
    public long getActiveAccounts() {
        return activeAccounts;
    }

    /**
     * Get total number of locked accounts.
     *
     * @return total number of locked accounts
     */
    public long getLockedAccounts() {
        return lockedAccounts;
    }
}
