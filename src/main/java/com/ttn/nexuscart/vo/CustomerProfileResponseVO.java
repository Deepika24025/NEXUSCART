package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerProfileResponseVO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String contact;
    private boolean isActive;
    private String image;
}
