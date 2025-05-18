package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressVO {
    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
}
