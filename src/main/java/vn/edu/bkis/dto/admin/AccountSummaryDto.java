package vn.edu.bkis.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Summary metrics rendered at the top of the admin account page.
 */
@Getter
@AllArgsConstructor
public class AccountSummaryDto {
    private final long totalAccounts;
    private final long adminAccounts;
    private final long teacherAccounts;
    private final long studentAccounts;
    private final long activeAccounts;
    private final long lockedAccounts;
}
