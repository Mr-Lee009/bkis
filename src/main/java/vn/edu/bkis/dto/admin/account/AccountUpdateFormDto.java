package vn.edu.bkis.dto.admin.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form data used to update an existing account from the modal dialog.
 */
@Getter
@Setter
@NoArgsConstructor
public class AccountUpdateFormDto {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    private String profilePictureUrl;
    private Boolean locked;
}
