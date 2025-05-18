package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PaginationRequestVO {
    private int max = 10;
    private int offset = 0;
    private String sort = "id";
    private String order = "asc";
    private Map<String,String> query = new HashMap<>();
}
