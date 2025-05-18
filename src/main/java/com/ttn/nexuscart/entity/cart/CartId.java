package com.ttn.nexuscart.entity.cart;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CartId {
    private UUID customerUserId;
    private UUID productVariationId;
}