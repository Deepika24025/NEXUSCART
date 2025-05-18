package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestCO {
    private String email;

    public LogoutRequestCO() {
    }

    public LogoutRequestCO(String email) {
        this.email = email;
    }
}
