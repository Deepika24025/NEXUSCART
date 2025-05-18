package com.ttn.nexuscart.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductResponseVO {
    private UUID id;
    private String name;
    private String description;
    private String brand;
    private boolean isActive;
    private boolean isCancellable;
    private boolean isReturnable;
    private String categoryName;
    private UUID categoryId;
}
