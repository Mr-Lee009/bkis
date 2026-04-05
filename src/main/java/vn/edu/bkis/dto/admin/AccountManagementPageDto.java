package vn.edu.bkis.dto.admin;

import java.util.List;
import vn.edu.bkis.model.UserRole;

/**
 * Composite DTO used to render the full admin account management page.
 */
public class AccountManagementPageDto {
    private final AccountSummaryDto summary;
    private final List<AccountRowDto> accounts;
    private final List<UserRole> roles;

    /**
     * Create the full page DTO.
     *
     * @param summary the page summary metrics
     * @param accounts the account rows for the table
     * @param roles the list of selectable roles
     */
    public AccountManagementPageDto(AccountSummaryDto summary, List<AccountRowDto> accounts, List<UserRole> roles) {
        this.summary = summary;
        this.accounts = accounts;
        this.roles = roles;
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
}
