package com.ttn.nexuscart.entity.order;

import com.ttn.nexuscart.entity.users.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(nullable = false)
    private String paymentMethod;

    @Column(name = "customer_address_city", nullable = false)
    private String customerAddressCity;

    @Column(name = "customer_address_state", nullable = false)
    private String customerAddressState;

    @Column(name = "customer_address_country", nullable = false)
    private String customerAddressCountry;

    @Column(name = "customer_address_address_line", nullable = false)
    private String customerAddressAddressLine;

    @Column(name = "customer_address_zip_code", nullable = false)
    private String customerAddressZipCode;

    @Column(name = "customer_address_label", nullable = false)
    private String customerAddressLabel;
}