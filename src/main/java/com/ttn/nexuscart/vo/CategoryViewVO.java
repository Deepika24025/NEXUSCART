package com.ttn.nexuscart.vo;

import com.ttn.nexuscart.co.CategoryMetadataFieldValueCO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryViewVO {
    private UUID categoryId;
    private String categoryName;
    private List<String> parentHierarchy;
    private List<String> childCategories;
    private List<CategoryMetadataFieldValueCO> metaField;

}
