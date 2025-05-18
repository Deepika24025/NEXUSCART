package com.ttn.nexuscart.co;

import com.ttn.nexuscart.entity.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SellerCO {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is mandatory")
    @Size(min = 6, max = 256, message = "Email must be between 6 and 256 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character"
    )
    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is mandatory")
    private String password;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character"
    )
    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is mandatory")
    private String confirmPassword;

    @NotBlank(message = "Company Name is required")
    private String companyName;

    @NotBlank(message = "Company Contact is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number should be 10 digits")
    private String companyContact;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
    message = "Gst number should follow a particular rule")
    @NotBlank(message = "GST number is required")
    private String gst;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Size(max = 1, message = "only one address allowed per seller")
    private Set<Address> address = new HashSet<>();
}
