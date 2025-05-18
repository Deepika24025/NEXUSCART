package com.ttn.nexuscart.vo;

import com.ttn.nexuscart.entity.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SellerVO {

    private UUID id;
    private String fullName;

    private String email;
    private Boolean isActive;
    private String companyName;
    //    private String companyAddress;
    private String companyContact;

    private Set<Address> address = new HashSet<>();

}
