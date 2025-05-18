package com.ttn.nexuscart.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProductVariationDetailsForCustomerVO {
    private UUID id;
    private BigDecimal price;
    private int quantityAvailable;
    private String  metadata;
    private String imageUrl;
}
