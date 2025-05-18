package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.category.CategoryMetadataField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, UUID> {
    Boolean existsByNameIgnoreCase(String name);

    //Page<CategoryMetadataField> findByNameContainingIgnoreCase(String query, Pageable pageable);

    Optional<CategoryMetadataField> findByName(String name);


}
