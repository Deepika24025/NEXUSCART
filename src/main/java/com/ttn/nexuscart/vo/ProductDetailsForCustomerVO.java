package com.ttn.nexuscart.vo;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDetailsForCustomerVO {
    private UUID id;
    private String name;
    private String brand;
    private String description;
    private CategoryViewVO category;
    private List<ProductVariationResponseVO> variations;
}
