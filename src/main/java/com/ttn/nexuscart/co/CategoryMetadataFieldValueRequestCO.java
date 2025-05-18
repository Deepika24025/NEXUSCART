package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CategoryMetadataFieldValueRequestCO {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Metadata Field ID is required")
    private UUID metadataFieldId;

    @NotEmpty(message = "At least one metadata value is required")
    private List<@NotEmpty(message = "Value cannot be empty") String> values;
}
