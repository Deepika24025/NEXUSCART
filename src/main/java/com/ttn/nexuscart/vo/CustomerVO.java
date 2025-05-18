package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerVO {
    private UUID id;
    private String fullName;
    private String email;
    private Boolean isActive;
}
