package com.ttn.nexuscart.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileResponseVO {
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String companyContact;
    private String companyName;
    private String image;
    private String gst;

    private AddressVO address;

}
