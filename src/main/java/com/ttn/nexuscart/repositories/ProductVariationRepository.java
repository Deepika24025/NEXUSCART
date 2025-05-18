package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.product.Product;
import com.ttn.nexuscart.entity.product.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {


    List<ProductVariation> findByProductId(UUID productId);

    Page<ProductVariation> findAllByProductId(UUID productId, Pageable pageable);

    List<ProductVariation> findByProductIdIn(List<UUID> productIds);

    Page<ProductVariation> findAllByProductIdAndProduct_NameContainingIgnoreCase(UUID productId, String productName, Pageable pageable);

    Page<ProductVariation> findAllByProductIdAndProductNameContainingIgnoreCase(UUID productId, String query, Pageable pageable);

    Page<ProductVariation> findAll(Specification<ProductVariation> specification, Pageable pageable);
}
