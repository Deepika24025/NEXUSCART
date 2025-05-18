package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByCategoryNameAndParentCategory(String categoryName, Category parentCategory);

    @Query("SELECT c FROM Category c WHERE c.categoryName = :name AND c.parentCategory IS NULL")
    Optional<Category> findRootCategoryByName(@Param("name") String name);

    List<Category> findByParentCategory(Category category);

    boolean existsByCategoryNameAndParentCategoryIsNull(String categoryName);

    boolean existsByCategoryNameAndParentCategoryIsNullAndIdNot(String name, UUID excludeId);

    boolean existsByCategoryNameAndParentCategoryIdAndIdNot(String name, UUID parentId, UUID excludeId);

    @Query("SELECT c FROM Category c WHERE c NOT IN (SELECT DISTINCT cat.parentCategory FROM Category cat WHERE cat.parentCategory IS NOT NULL)")
    List<Category> findAllLeafCategories();

    Collection<Object> findByParentCategoryId(UUID id);

    List<Category> findAllByParentCategoryIsNull();

    List<Category> findAllByParentCategory(Category parent);

    boolean existsByParentCategory(Category category);

    List<Category> findByParentCategory_Id(UUID parentId);
    Page<Category> findByCategoryNameContainingIgnoreCase(String name, Pageable pageable);


}

