package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ProductVariationRequestCO {

    private UUID productId;
    private Integer quantityAvailable;
    private BigDecimal price;
    private MultipartFile primaryImage;
    private List<MultipartFile> secondaryImages;
    private Map<String, String> metadata;
    private Boolean isActive;
}
