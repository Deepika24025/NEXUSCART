package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductFilterResponseVO {
    private List<ProductDetailForCustomerVO> products;
    private Map<String, List<String>> metadataFields;
    private List<String> brands;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
