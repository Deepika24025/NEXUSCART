package com.ttn.nexuscart.entity.order;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.ttn.nexuscart.enums.OrderStatus fromStatus;  // Previous status

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.ttn.nexuscart.enums.OrderStatus toStatus;  // New status

    private String transitionNotes;  // Comments for tracking

    @Column(nullable = false)
    private LocalDateTime transitionDate = LocalDateTime.now();  // Timestamp

    @OneToOne
    @JoinColumn(name = "order_product_id", nullable = false)
    private OrderProduct orderProduct;
}
