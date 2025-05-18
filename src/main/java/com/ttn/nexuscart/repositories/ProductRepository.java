package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.category.Category;
import com.ttn.nexuscart.entity.product.Product;
import com.ttn.nexuscart.entity.users.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>,JpaSpecificationExecutor<Product> {
    boolean existsByNameAndBrandAndCategoryAndSeller(String name, String brand, Category category, Seller seller);


    Page<Product> findBySellerAndIsDeletedFalseAndNameContainingIgnoreCase(
             Seller seller,
             String name,
            Pageable pageable
    );
    Optional<Product> findByIdAndSeller(UUID id, Seller seller);

    Page<Product> findBySeller(Seller seller, Pageable pageable);

    boolean existsByNameIgnoreCaseAndBrandAndCategoryAndSeller(
            String name, String brand, Category category, Seller seller
    );

    Optional<Product>findByIdAndIsDeletedFalse(UUID productId);

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    List<Product> findByCategoryIdInAndIsDeletedFalseAndIsActiveTrue(List<UUID> categoryIds);
    List<Product> findAllByIsActiveTrueAndIsDeletedFalseAndCategoryIdIn(List<UUID> categoryIds);


}
