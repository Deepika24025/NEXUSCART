package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductRequestCO {
    @NotBlank(message = "product name is required")
    private String name;
    @NotBlank(message = "Brand name is required")
    private String brand;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Id is required")
    private UUID categoryId;
    private boolean isCancellable;
    private boolean isReturnable;
}
