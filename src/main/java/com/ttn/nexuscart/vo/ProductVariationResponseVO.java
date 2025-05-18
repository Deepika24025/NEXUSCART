package com.ttn.nexuscart.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ProductVariationResponseVO {
    private UUID id;
    private BigDecimal price;
    private Integer quantityAvailable;
    private String metadata;
    private String primaryImage;
    private UUID productId;
    private Boolean isActive;
    private String productName;
    private String productDescription;
}
