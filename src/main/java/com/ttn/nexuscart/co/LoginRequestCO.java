package com.ttn.nexuscart.co;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestCO {
    @Email(message = "Email format is Invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password required")
    private String password;
}

