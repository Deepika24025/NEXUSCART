package com.ttn.nexuscart.entity.product;

import com.ttn.nexuscart.entity.users.Customer;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
public class ProductReview {

    @EmbeddedId
    ProductReviewId productReviewId;

    @ManyToOne
    @MapsId(value = "customerUserId")
    private Customer customer;

    private String review;

    private Double rating;

    @ManyToOne
    @MapsId(value = "productId")
    private Product product;
}
