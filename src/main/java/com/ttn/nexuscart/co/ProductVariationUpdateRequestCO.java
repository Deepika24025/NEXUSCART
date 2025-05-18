package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ProductVariationUpdateRequestCO {
    private Integer quantityAvailable;
    private BigDecimal price;
    private MultipartFile primaryImage;
    private Map<String, String> metadata;
    private Boolean isActive;
}
