package vn.edu.bkis.dto;

import lombok.Data;

@Data
public class ResetPasswordFormDto {
    private String token;
    private String password;
    private String confirmPassword;
}
