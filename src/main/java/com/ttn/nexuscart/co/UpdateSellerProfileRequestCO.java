package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSellerProfileRequestCO {
    private String firstName;
    private String lastName;
    private String companyContact;
    private String companyName;
    private String gst;
}
