package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateCategoryRequestCO {

    @NotNull(message = "category id is required")
    private UUID id;

    @NotBlank(message = "category name required")
    private String name;
}
