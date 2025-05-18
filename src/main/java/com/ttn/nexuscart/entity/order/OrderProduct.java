package com.ttn.nexuscart.entity.order;

import com.ttn.nexuscart.entity.product.ProductVariation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_product")
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;

    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @OneToOne(mappedBy = "orderProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderStatus orderStatus;
}