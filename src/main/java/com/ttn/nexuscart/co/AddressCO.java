package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCO {
    @NotBlank(message = "City cannot be blank")
    private String city;
    @NotBlank(message = "State cannot be blank")
    private String state;
    @NotBlank(message = "Country cannot be blank")
    private String country;
    @NotBlank(message = "Address Line cannot be blank")
    private String addressLine;
    private String label;
    @NotBlank(message = "Zip Code cannot be blank")
    private String zipCode;
}
