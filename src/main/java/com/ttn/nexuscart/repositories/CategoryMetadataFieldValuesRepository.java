package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.category.Category;
import com.ttn.nexuscart.entity.category.CategoryMetadataField;
import com.ttn.nexuscart.entity.category.CategoryMetadataFieldValues;
import com.ttn.nexuscart.entity.category.CategoryMetadataFieldValuesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryMetadataFieldValuesRepository extends JpaRepository<CategoryMetadataFieldValues, CategoryMetadataFieldValuesId> {

    List<CategoryMetadataFieldValues> findByCategory(Category category);

    Optional<CategoryMetadataFieldValues> findByCategoryAndMetadataField(Category category, CategoryMetadataField metadataField);

    boolean existsByCategoryAndMetadataField(Category category, CategoryMetadataField field);

    List<CategoryMetadataFieldValues> findByCategoryIdIn(List<UUID> categoryIds);
}

