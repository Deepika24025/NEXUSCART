package com.ttn.nexuscart.entity.product;

import com.ttn.nexuscart.entity.category.Category;
import com.ttn.nexuscart.entity.users.Seller;
import com.ttn.nexuscart.security.config.AuditMetaData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;


@Entity
@Table(name = "product")
@Getter
@Setter
public class Product extends AuditMetaData {
    private boolean isDeleted = false;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false) // Foreign Key
    private Seller seller;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private String brand;
    private Boolean isActive;
}
