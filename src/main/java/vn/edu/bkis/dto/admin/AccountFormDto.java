package vn.edu.bkis.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form data used to create a new account from the admin screen.
 */
@Getter
@Setter
@NoArgsConstructor
public class AccountFormDto {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String bio;
    private String profilePictureUrl;
    private String role;
}
