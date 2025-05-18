package com.ttn.nexuscart.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRefreshResponseVO {
    private String accessToken;
    private String message;
}
