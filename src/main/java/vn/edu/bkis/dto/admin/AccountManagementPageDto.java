package vn.edu.bkis.dto.admin;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.edu.bkis.model.UserRole;

/**
 * Composite DTO used to render the full admin account management page.
 */
@Getter
@Setter
public class AccountManagementPageDto {
    private final AccountSummaryDto summary;
    private final List<AccountRowDto> accounts;
    private final List<UserRole> roles;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    /**
     * Create the full page DTO.
     *
     * @param summary the page summary metrics
     * @param accounts the account rows for the table
     * @param roles the list of selectable roles
     * @param currentPage the current page index
     * @param pageSize the selected page size
     * @param totalPages the total number of pages
     * @param totalItems the total number of filtered rows
     */
    public AccountManagementPageDto(AccountSummaryDto summary, List<AccountRowDto> accounts, List<UserRole> roles,
                                    int currentPage, int pageSize, int totalPages, long totalItems) {
        this.summary = summary;
        this.accounts = accounts;
        this.roles = roles;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    /**
     * Get the page summary metrics.
     *
     * @return the page summary metrics
     */
    public AccountSummaryDto getSummary() {
        return summary;
    }

    /**
     * Get the account rows for the table.
     *
     * @return the account rows
     */
    public List<AccountRowDto> getAccounts() {
        return accounts;
    }

    /**
     * Get the selectable roles for filter and form controls.
     *
     * @return the selectable roles
     */
    public List<UserRole> getRoles() {
        return roles;
    }

    /**
     * Get the current page index.
     *
     * @return the current page index
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Get the selected page size.
     *
     * @return the selected page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get the total number of pages.
     *
     * @return the total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Get the total number of filtered rows.
     *
     * @return the total number of filtered rows
     */
    public long getTotalItems() {
        return totalItems;
    }

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
