package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryRequestCO {
    @NotBlank(message = "category name cannot be blank")
    private String categoryName;

    private UUID parentId;
}
