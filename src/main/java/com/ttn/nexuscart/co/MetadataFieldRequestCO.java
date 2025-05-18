package com.ttn.nexuscart.co;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataFieldRequestCO {
    @NotBlank(message = "Name is required")
    private String name;
}
