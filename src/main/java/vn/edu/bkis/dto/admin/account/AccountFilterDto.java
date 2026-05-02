package vn.edu.bkis.dto.admin.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter data used by the admin account list screen.
 */
@Getter
@Setter
@NoArgsConstructor
public class AccountFilterDto {
    private String keyword;
    private String role;
    private Integer page;
    private Integer size;
}
