package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CustomerCategoryResponseVO {
    private UUID id;
    private String categoryName;
    private List<CustomerMetadataFieldVO> metadataFields;
}
