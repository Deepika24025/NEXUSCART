package com.ttn.nexuscart.service;

import com.ttn.nexuscart.co.*;
import com.ttn.nexuscart.vo.*;
import com.ttn.nexuscart.entity.category.Category;
import com.ttn.nexuscart.entity.category.CategoryMetadataField;
import com.ttn.nexuscart.entity.category.CategoryMetadataFieldValues;
import com.ttn.nexuscart.entity.category.CategoryMetadataFieldValuesId;
import com.ttn.nexuscart.entity.product.Product;
import com.ttn.nexuscart.entity.product.ProductVariation;
import com.ttn.nexuscart.exceptions.DuplicateResourceException;
import com.ttn.nexuscart.exceptions.InvalidInputException;
import com.ttn.nexuscart.exceptions.ResourceNotFoundException;
import com.ttn.nexuscart.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;
     @Autowired
   private   ProductService productService;

    @Autowired
    private ProductVariationRepository productVariationRepository;
    @Autowired
    private CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;

    public UUID addMetadataField(MetadataFieldRequestCO metadataFieldRequestCO) {
        if (categoryMetadataFieldRepository.existsByNameIgnoreCase(metadataFieldRequestCO.getName())) {
            throw new IllegalArgumentException("Metadata field with this name already exists");
        }

        CategoryMetadataField categoryMetadataField = new CategoryMetadataField();
        categoryMetadataField.setName(metadataFieldRequestCO.getName());
        categoryMetadataField.setMetadataValues(new ArrayList<>());

        CategoryMetadataField savedField = categoryMetadataFieldRepository.save(categoryMetadataField);
        return savedField.getId();
    }

    public List<CategoryMetadataField> viewMetadataField(int offset, int max, String sort, String order, String query) {
        PageRequest pageable = order.equalsIgnoreCase("DESC") ?
                PageRequest.of(offset, max, Sort.by(sort).descending()) :
                PageRequest.of(offset, max, Sort.by(sort).ascending());

        if (query != null && !query.isBlank()) {
            String[] queryFields = query.split(",");
            List<CategoryMetadataField> matchedFields = new ArrayList<>();

            for (String field : queryFields) {
                categoryMetadataFieldRepository.findByName(field)
                        .ifPresent(matchedFields::add);
            }

            return matchedFields;
        }

        return categoryMetadataFieldRepository.findAll(pageable).getContent();
    }

    public ResponseEntity<String> addCategory(CategoryRequestCO request, UUID parentId) {
        Category category = new Category();

        if (parentId == null) {
            if (categoryRepository.existsByCategoryNameAndParentCategoryIsNull(request.getCategoryName())) {
                throw new DuplicateResourceException("Category already exists");
            }
            category.setCategoryName(request.getCategoryName());
        } else {
            if (!categoryRepository.existsById(parentId)) {
                throw new ResourceNotFoundException("Parent Id does not exist");
            }
            if (categoryRepository.existsByCategoryNameAndParentCategory(request.getCategoryName(), categoryRepository.findById(parentId).get())) {
                throw new RuntimeException("Category and Parent Category already mapped");
            }

            String name = request.getCategoryName();
            UUID parent = parentId;

            while (true) {
                Optional<Category> optionalCategory = categoryRepository.findById(parent);
                if (optionalCategory.isEmpty()) {
                    throw new InvalidInputException("Invalid parent category");
                }
                Category parentCategory = optionalCategory.get();
                if (parentCategory.getCategoryName().equals(name)) {
                    throw new DuplicateResourceException("Category already exists");
                }
                if (parentCategory.getParentCategory() == null) {
                    break;
                }
                parent = parentCategory.getParentCategory().getId();
            }

            category.setCategoryName(request.getCategoryName());
            category.setParentCategory(categoryRepository.findById(parentId).get());
        }

        // Save and capture the saved entity
        Category savedCategory = categoryRepository.save(category);

        return ResponseEntity.ok("Category added successfully with id " + savedCategory.getId());
    }

    public CategoryResponseVO viewCategoryDetails(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category ID does not exist"));

        CategoryResponseVO response = new CategoryResponseVO();
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getCategoryName());

        // 1. Build parent hierarchy up to root
        List<String> parentHierarchy = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentHierarchy.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentHierarchy); // So it's from root -> immediate parent
        response.setParentHierarchy(parentHierarchy);

        // 2. Get immediate children
        List<String> children = categoryRepository.findByParentCategory(category)
                .stream()
                .map(Category::getCategoryName)
                .toList();
        response.setChildCategories(children);

        // 3. Metadata values
        List<CategoryMetadataFieldValueCO> metadata = categoryMetadataFieldValuesRepository.findByCategory(category)
                .stream()
                .map(value -> {
                    CategoryMetadataFieldValueCO co = new CategoryMetadataFieldValueCO();
                    co.setFieldName(value.getMetadataField().getName());
                    co.setValues(List.of(value.getValue().split(","))); // assuming values are comma-separated
                    return co;
                })
                .toList();
        response.setMetadataValues(metadata);

        return response;
    }


    public List<CategoryResponseVO> getAllCategories(int max, int offset, String sort, String order, String query) {
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort));

        Page<Category> categoryPage;

        if (query != null && !query.isEmpty()) {
            categoryPage = categoryRepository.findByCategoryNameContainingIgnoreCase(query, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        List<CategoryResponseVO> categoryResponseVOList = new ArrayList<>();
        for (Category temp : categoryPage.getContent()) {
            CategoryResponseVO categoryResponseVO = viewCategoryDetails(temp.getId());
            categoryResponseVOList.add(categoryResponseVO);
        }
        return categoryResponseVOList;
    }


    public ResponseEntity<String> updateCategory(UpdateCategoryRequestCO updateCategoryDto) {
        if (!categoryRepository.existsById(updateCategoryDto.getId())) {
            throw new ResourceNotFoundException("Category Id not exists");
        }
        Category category = categoryRepository.findById(updateCategoryDto.getId()).get();
        Category temp = category;
        while (temp != null) {

            if (temp.getCategoryName().equals(updateCategoryDto.getName())) {
                throw new DuplicateResourceException("Can not be updated category already exists");
            }
            temp = temp.getParentCategory();
        }
        if (categoryRepository.existsByCategoryNameAndParentCategory(updateCategoryDto.getName(), category.getParentCategory())) {
            throw new DuplicateResourceException("Can not be updated category and parent category already mapped");
        }
        List<Category> categories = categoryRepository.findByParentCategory(category);
        while (!categories.isEmpty()) {
            Category category1 = categories.get(0);
            if (category1.getCategoryName().equals(updateCategoryDto.getName())) {
                throw new DuplicateResourceException("Category already exist in children");
            }
            categories.remove(0);
            List<Category> categoryList = categoryRepository.findByParentCategory(category1);
            if (!categoryList.isEmpty()) categories.addAll(categoryList);
        }
        category.setCategoryName(updateCategoryDto.getName());
        categoryRepository.save(category);
        return ResponseEntity.ok("Category updated successfully");
    }

    public String addMetadataFieldValues(CategoryMetadataFieldValueRequestCO co) {
        Category category = categoryRepository.findById(co.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        CategoryMetadataField field = categoryMetadataFieldRepository.findById(co.getMetadataFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Metadata field not found"));

        //  Check if already exists
        boolean exists = categoryMetadataFieldValuesRepository.existsByCategoryAndMetadataField(category, field);
        if (exists) {
            throw new ResourceNotFoundException("Metadata for this field already exists for this category");
        }

        //  Ensure uniqueness in input
        List<String> uniqueValues = co.getValues().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        //  Join to comma-separated string
        String combinedValue = String.join(",",uniqueValues);

        //  Save
        CategoryMetadataFieldValues values = new CategoryMetadataFieldValues();
        values.setId(new CategoryMetadataFieldValuesId(field.getId(), category.getId()));
        values.setMetadataField(field);
        values.setCategory(category);
        values.setValue(combinedValue);

        categoryMetadataFieldValuesRepository.save(values);

        return "Metadata values saved successfully";
    }


    public String updateMetadataFieldValues(CategoryMetadataFieldValueRequestCO co) {
        Category category = categoryRepository.findById(co.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        CategoryMetadataField field = categoryMetadataFieldRepository.findById(co.getMetadataFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Metadata field not found"));

        CategoryMetadataFieldValuesId compositeId = new CategoryMetadataFieldValuesId(field.getId(), category.getId());

        CategoryMetadataFieldValues existing = categoryMetadataFieldValuesRepository.findById(compositeId)
                .orElseThrow(() -> new RuntimeException("This metadata field is not associated with the category"));

        // Get existing values
        Set<String> existingValues = Arrays.stream(existing.getValue().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Clean and add new values
        for (String val : co.getValues()) {
            if (!existingValues.add(val.trim().toLowerCase())) {
                throw new DuplicateResourceException("Duplicate value already exists: " + val);
            }
        }

        String updatedValue = String.join(", ", existingValues);
        existing.setValue(updatedValue);
        categoryMetadataFieldValuesRepository.save(existing);

        return "Metadata values updated successfully";
    }


    public List<CategoryViewVO> getAllLeafCategoriesWithMetadata() {
        List<Category> allCategories = categoryRepository.findAll();

        return allCategories.stream()
                .filter(category -> categoryRepository.findByParentCategoryId(category.getId()).isEmpty()) // leaf only
                .map(this::getCategoryDetails)
                .toList();
    }


    public CategoryViewVO getCategoryDetails(Category category) {
        // Build parent hierarchy
        List<String> parentHierarchy = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentHierarchy.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentHierarchy);

        // Metadata fields and values as List
        List<CategoryMetadataFieldValueCO> metadataFields = category.getMetadataValues().stream()
                .collect(Collectors.groupingBy(val -> val.getMetadataField().getName()))
                .entrySet().stream()
                .map(entry -> new CategoryMetadataFieldValueCO(entry.getKey(),
                        entry.getValue().stream()
                                .map(CategoryMetadataFieldValues::getValue)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return new CategoryViewVO(
                category.getId(),
                category.getCategoryName(),
                parentHierarchy,
                new ArrayList<>(),
                metadataFields
        );
    }


    //customer

    public List<CustomerCategoryResponseVO> getCategories(UUID categoryId) {
        List<Category> categories;

        if (categoryId == null) {
            // Root level categories (no parent)
            categories = categoryRepository.findAllByParentCategoryIsNull();
        } else {
            // Validate categoryId
            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new InvalidInputException("Invalid category ID"));
            categories = categoryRepository.findAllByParentCategory(parent);
        }

        return categories.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private CustomerCategoryResponseVO mapToDTO(Category category) {
        CustomerCategoryResponseVO dto = new CustomerCategoryResponseVO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());

        List<CustomerMetadataFieldVO> metadata = category.getMetadataValues().stream()
                .map(value -> new CustomerMetadataFieldVO(value.getMetadataField().getName(), value.getValue()))
                .collect(Collectors.toList());

        dto.setMetadataFields(metadata);
        return dto;
    }



    public ProductFilterResponseVO getProductsByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Include child category IDs
        List<UUID> categoryIds = new ArrayList<>();
        List<Category> children = productService.findAllChildrenRecursively(category.getId());
        categoryIds.addAll(children.isEmpty() ? List.of(category.getId()) : children.stream().map(Category::getId).toList());

        // Fetch active, non-deleted products
        List<Product> products = productRepository.findAllByIsActiveTrueAndIsDeletedFalseAndCategoryIdIn(categoryIds);

        List<UUID> productIds = products.stream().map(Product::getId).toList();

        // Fetch product variations
        List<ProductVariation> variations = productVariationRepository.findByProductIdIn(productIds);

        // Extract metadata fields + values
        List<CategoryMetadataFieldValues> fieldValues = categoryMetadataFieldValuesRepository.findByCategoryIdIn(categoryIds);
        Map<String, Set<String>> metadataMap = new HashMap<>();
        for (CategoryMetadataFieldValues val : fieldValues) {
            String fieldName = val.getMetadataField().getName();
            List<String> values = Arrays.asList(val.getValue().split(","));
            metadataMap.computeIfAbsent(fieldName, k -> new HashSet<>()).addAll(values);
        }

        // Extract brands
        Set<String> brands = products.stream().map(Product::getBrand).collect(Collectors.toSet());

        // Extract min/max price
        BigDecimal minPrice = variations.stream().map(ProductVariation::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = variations.stream().map(ProductVariation::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        // Convert to VO
        List<ProductDetailForCustomerVO> productVOs = products.stream()
                .map(p -> productService.viewProductForCustomer(p.getId()))
                .toList();

        // Response DTO
        ProductFilterResponseVO response = new ProductFilterResponseVO();
        response.setProducts(productVOs);
        response.setMetadataFields(metadataMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()))));
        response.setBrands(new ArrayList<>(brands));
        response.setMinPrice(minPrice);
        response.setMaxPrice(maxPrice);

        return response;
    }


}