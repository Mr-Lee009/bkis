package vn.edu.bkis.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.bkis.model.UserRole;

/**
 * Composite DTO used to render the full admin account management page.
 */
@Getter
@AllArgsConstructor
public class AccountManagementPageDto {
    private final AccountSummaryDto summary;
    private final List<AccountRowDto> accounts;
    private final List<UserRole> roles;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    /**
     * Check whether a previous page exists.
     *
     * @return true when the previous page exists
     */
    public boolean isHasPrevious() {
        return currentPage > 0;
    }

    /**
     * Check whether a next page exists.
     *
     * @return true when the next page exists
     */
    public boolean isHasNext() {
        return currentPage + 1 < totalPages;
    }

    /**
     * Get the one-based page number for display.
     *
     * @return the one-based page number
     */
    public int getDisplayPage() {
        return currentPage + 1;
    }

    /**
     * Get the current page start row for display.
     *
     * @return the current page start row
     */
    public long getStartRow() {
        return totalItems == 0 ? 0 : ((long) currentPage * pageSize) + 1;
    }

    /**
     * Get the current page end row for display.
     *
     * @return the current page end row
     */
    public long getEndRow() {
        return Math.min(totalItems, (long) (currentPage + 1) * pageSize);
    }
}
