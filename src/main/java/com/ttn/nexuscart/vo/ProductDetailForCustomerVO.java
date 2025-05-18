package com.ttn.nexuscart.vo;

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
public class ProductDetailForCustomerVO {
    private UUID id;
    private String name;
    private String brand;
    private String description;
    private CategoryDetailsVO category;
    private List<ProductVariationDetailsForCustomerVO> variations;
}
