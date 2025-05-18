package com.ttn.nexuscart.vo;

import com.ttn.nexuscart.co.CategoryMetadataFieldValueCO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CategoryResponseVO {
    private UUID categoryId;
    private String categoryName;
    private List<String> parentHierarchy;     // parent categories up to root
    private List<String> childCategories;     // immediate children
    private List<CategoryMetadataFieldValueCO> metadataValues;
}
