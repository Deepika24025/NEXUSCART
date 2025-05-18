package com.ttn.nexuscart.co;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateProductRequestCO {
    @NonNull()
    private UUID productId;

    private String name;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;

}
