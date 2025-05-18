package com.ttn.nexuscart.co;

import com.ttn.nexuscart.entity.Address;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

public class CustomerCO {

    @Email(message = "Invalid Email")
    @NotBlank(message = "Email is required")
    @Size(min = 6, max = 256, message = "Email must be between 6 and 256 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;
    @NotBlank(message = "FirstName is required")
    private String firstName;
    private String middleName;
    @NotBlank(message = "LastName is required")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character"
    )
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is mandatory")
    private String confirmPassword;
    @Size(min = 10, max = 10, message = "Contact number must be of 10 digits")
    private String contact;

    private Set<Address> addresses = new HashSet<>();


}
