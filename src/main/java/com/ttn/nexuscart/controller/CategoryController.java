package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.co.*;
import com.ttn.nexuscart.vo.CategoryResponseVO;
import com.ttn.nexuscart.vo.CategoryViewVO;
import com.ttn.nexuscart.vo.CustomerCategoryResponseVO;
import com.ttn.nexuscart.vo.ProductFilterResponseVO;
import com.ttn.nexuscart.entity.category.CategoryMetadataField;
import com.ttn.nexuscart.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-metadata-field")
    public ResponseEntity<String> addMetadataField(@Valid @RequestBody MetadataFieldRequestCO metadataFieldRequestCO) {
        UUID id = categoryService.addMetadataField(metadataFieldRequestCO);
        return ResponseEntity.status(HttpStatus.CREATED).body("MetadataField created with id " + id);
    }

    @GetMapping("/get-metadata-field")
    public ResponseEntity<List<CategoryMetadataField>> viewMetadataField(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String query) {

        List<CategoryMetadataField> fields = categoryService.viewMetadataField(offset, max, sort, order, query);
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/add-category")
    public ResponseEntity<String> addCategory(@RequestBody CategoryRequestCO request, @RequestParam(required = false) UUID parentId) {
        return categoryService.addCategory(request, parentId);
    }

    @GetMapping("/view-category")
    public ResponseEntity<CategoryResponseVO> viewCategory(@RequestBody CategoryIdRequestCO categoryIdRequestCO) {
        CategoryResponseVO response = categoryService.viewCategoryDetails(categoryIdRequestCO.getCategoryId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-all-categories")
    public ResponseEntity<List<CategoryResponseVO>> getAllCategories(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "categoryName") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {
        List<CategoryResponseVO> categories = categoryService.getAllCategories(max, offset, sort, order, query);
        return ResponseEntity.ok(categories);
    }


    @PutMapping("/update-category")
    public ResponseEntity<String> updateCategory(@RequestBody UpdateCategoryRequestCO updateCategoryDto) {
        return categoryService.updateCategory(updateCategoryDto);
    }


    @PostMapping("/add-category-metadata-values")

    public ResponseEntity<String> addCategoryMetadataValues(
            @RequestBody @Valid CategoryMetadataFieldValueRequestCO co) {
        String result = categoryService.addMetadataFieldValues(co);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/update-category-metadata-values")
    public ResponseEntity<String> updateCategoryMetadataValues(@RequestBody CategoryMetadataFieldValueRequestCO request) {
        try {
            String response = categoryService.updateMetadataFieldValues(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/leaf-categories")
    public ResponseEntity<List<CategoryViewVO>> getLeafCategoriesWithMetadata() {
        List<CategoryViewVO> categories = categoryService.getAllLeafCategoriesWithMetadata();
        return ResponseEntity.ok(categories);
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/customer-getcategories")
    public ResponseEntity<?> getCategories(@RequestParam(required = false) UUID categoryId) {
        List<CustomerCategoryResponseVO> categoryVO = categoryService.getCategories(categoryId);
        return ResponseEntity.ok(categoryVO);
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/products/filter/{categoryId}")
    public ResponseEntity<ProductFilterResponseVO> getFilteredProductsForCustomer(
            @PathVariable UUID categoryId
    ) {
        ProductFilterResponseVO response = categoryService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

}

