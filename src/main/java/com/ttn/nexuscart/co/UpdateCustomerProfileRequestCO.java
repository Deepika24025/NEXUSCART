package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequestCO {


    private String firstName;

    private String middleName;

    private String lastName;

  //  @Pattern(regexp = "^\\d{10,}$", message = "Contact number must be at least 10 digits")
    private String contact;


}
