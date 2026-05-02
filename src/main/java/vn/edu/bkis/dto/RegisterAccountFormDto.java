package vn.edu.bkis.dto;

import lombok.Data;

@Data
public class RegisterAccountFormDto {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
}
