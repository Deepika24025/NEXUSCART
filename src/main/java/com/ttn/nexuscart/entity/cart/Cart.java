package com.ttn.nexuscart.entity.cart;


import com.ttn.nexuscart.entity.product.ProductVariation;
import com.ttn.nexuscart.entity.users.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @EmbeddedId
    private CartId id;

    @OneToOne
    @MapsId("customerUserId")
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean isWishlistItem;

    @OneToOne
    @MapsId("productVariationId")
    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;

    public Cart(Customer customer, ProductVariation productVariation, int quantity) {
        this.id = new CartId(customer.getId(), productVariation.getId());
        this.customer = customer;
        this.productVariation = productVariation;
        this.quantity = quantity;
    }

}
