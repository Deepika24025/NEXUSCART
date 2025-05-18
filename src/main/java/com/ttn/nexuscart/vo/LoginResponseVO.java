package com.ttn.nexuscart.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseVO {
    private String accessToken;
    private String refreshToken;
    private String message;
}
