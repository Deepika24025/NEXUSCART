package com.ttn.nexuscart.co;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequestCO {
    private String token;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character"
    )
    private String newPassword;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character"
    )
    private String confirmPassword;

}
