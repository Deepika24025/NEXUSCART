package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestCO {
    @NotBlank(message = "email is required")
    private String email;
}
