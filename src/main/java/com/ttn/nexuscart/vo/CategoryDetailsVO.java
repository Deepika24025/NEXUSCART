package com.ttn.nexuscart.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailsVO {
    private UUID id;
    private String name;
    private List<String> parentHierarchy;
}
