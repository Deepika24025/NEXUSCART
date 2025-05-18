package com.ttn.nexuscart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.nexuscart.co.*;
import com.ttn.nexuscart.vo.*;
import com.ttn.nexuscart.entity.category.Category;
import com.ttn.nexuscart.entity.category.CategoryMetadataField;
import com.ttn.nexuscart.entity.category.CategoryMetadataFieldValues;
import com.ttn.nexuscart.entity.product.Product;
import com.ttn.nexuscart.entity.product.ProductVariation;
import com.ttn.nexuscart.entity.users.Seller;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.exceptions.DuplicateResourceException;
import com.ttn.nexuscart.exceptions.ResourceNotFoundException;
import com.ttn.nexuscart.exceptions.UnauthorisedAccessException;
import com.ttn.nexuscart.exceptions.UserNotFoundException;
import com.ttn.nexuscart.repositories.*;
import com.ttn.nexuscart.util.FileStorageUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;
    @Autowired
    private CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    public void addProduct(ProductRequestCO dto, Seller seller) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean hasChildren = categoryRepository.existsByParentCategory(category);
        if (hasChildren) {
            throw new RuntimeException("Category must be a leaf node");
        }

        Seller persistedSeller = sellerRepository.findById(seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (productRepository.existsByNameAndBrandAndCategoryAndSeller(
                dto.getName(), dto.getBrand(), category, persistedSeller)) {
            throw new DuplicateResourceException("Product already exists with same name-brand-category for this seller");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(email + "Email");
        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setSeller(sellerRepository.findByEmail(email).get());
        product.setIsCancellable(dto.isCancellable());
        product.setIsReturnable(dto.isReturnable());
        product.setIsActive(false);


        productRepository.save(product);
        String adminEmail = "deepika.rani@tothenew.com";
        String body = "A new product called " + product.getName() + "with description " + product.getDescription()+" and is awaitng your approval";
        String subject = "New product added";
        emailService.sendEmail(adminEmail,subject,body);

    }


    public ProductResponseVO viewProductById(UUID productId, String email) {
        System.out.println("Fetching product by ID: " + productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Product has been deleted");
        }

        System.out.println("Fetching seller by email: " + email);
        Seller seller = (Seller) userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with email: " + email));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorisedAccessException("Unauthorized: product belongs to seller " + product.getSeller().getId());
        }

        ProductResponseVO dto = new ProductResponseVO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setActive(product.getIsActive());
        dto.setCancellable(product.getIsCancellable());
        dto.setReturnable(product.getIsReturnable());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getCategoryName());

        return dto;
    }


    public List<ProductResponseVO> viewAllProducts(
            Seller loggedInSeller,
            int max,
            int offset,
            String sort,
            String order,
            String query
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sort != null ? sort : "name"));

        Specification<Product> specification = (root, queryObj, cb) -> {
            // Seller filter
            Predicate sellerPredicate = cb.equal(root.get("seller"), loggedInSeller);

            // Not deleted filter
            Predicate notDeletedPredicate = cb.isFalse(root.get("isDeleted"));

            // Query filter (search by name)
            Predicate queryPredicate = null;
            if (query != null && !query.trim().isEmpty()) {
                String trimmedQuery = "%" + query.trim().toLowerCase() + "%";
                queryPredicate = cb.like(cb.lower(root.get("name")), trimmedQuery); // Case-insensitive name search
            }

            // Combine predicates
            if (queryPredicate != null) {
                return cb.and(sellerPredicate, notDeletedPredicate, queryPredicate);
            } else {
                return cb.and(sellerPredicate, notDeletedPredicate); // No query filter
            }
        };

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        return productPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private ProductResponseVO convertToResponseDto(Product product) {
        ProductResponseVO response = new ProductResponseVO();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCancellable(product.getIsCancellable());
        response.setReturnable(product.getIsReturnable());
        response.setActive(product.getIsActive());
        response.setCategoryName(product.getCategory().getCategoryName());
        response.setCategoryId(product.getCategory().getId());
        return response;
    }


    public void deleteProduct(UUID productId, String email) {
        Seller seller = sellerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with email: " + email));

        Product product = productRepository.findByIdAndSeller(productId, seller)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " for the logged-in seller"));

        if (product.isDeleted()) {
            throw new IllegalStateException("Product is already deleted");
        }

        product.setDeleted(true); // Soft delete
        productRepository.save(product);
    }

    public void updateProduct(UpdateProductRequestCO dto, String email) {
        Seller seller = sellerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with email: " + email));

        Product product = productRepository.findByIdAndSeller(dto.getProductId(), seller)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(product.getName())) {
            boolean nameExists = productRepository.existsByNameIgnoreCaseAndBrandAndCategoryAndSeller(
                    dto.getName(), product.getBrand(), product.getCategory(), seller
            );
            if (nameExists) {
                throw new IllegalArgumentException("Product name already exists for this brand, category, and seller.");
            }
            product.setName(dto.getName());
        }

        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getIsCancellable() != null) product.setIsCancellable(dto.getIsCancellable());
        if (dto.getIsReturnable() != null) product.setIsReturnable(dto.getIsReturnable());

        productRepository.save(product);
    }

    public ResponseEntity<String> addProductVariation(ProductVariationRequestCO productVariationRequestCO, Seller seller) {
        Product product = productRepository.findById(productVariationRequestCO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found"));

        if (!product.getIsActive() || product.isDeleted()) {
            throw new RuntimeException("Product is not active or has been deleted");
        }

        if (ifCategoryAndMetaFieldValuesNotExist(product, productVariationRequestCO.getMetadata())) {
            throw new RuntimeException("Category and Meta field values do not exist");
        }

        ObjectMapper mapper = new ObjectMapper();

        if (ifStructureIsNotSame(mapper, product, productVariationRequestCO.getMetadata().keySet())) {
            throw new RuntimeException("The structure of the variation should be the same");
        }

        if (ifVariationIsSame(mapper, product, productVariationRequestCO.getMetadata())) {
            throw new DuplicateResourceException("This type of variation already exists");
        }

        if (ifImageNotInCorrectFormat(productVariationRequestCO.getPrimaryImage())) {
            throw new RuntimeException("Image format is not correct");
        }

        ProductVariation productVariation = setProductVariation(productVariationRequestCO, product, mapper);
        productVariationRepository.save(productVariation);

        return ResponseEntity.ok("Product Variation added Successfully");
    }

    private ProductVariation setProductVariation(ProductVariationRequestCO dto, Product product, ObjectMapper mapper) {
        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setPrice(dto.getPrice());
        variation.setQuantityAvailable(dto.getQuantityAvailable());

        try {
            String image = fileStorageUtil.saveImage(dto.getPrimaryImage(),product.getId());
            variation.setPrimaryImageName(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }

        try {
            String metadataJson = mapper.writeValueAsString(dto.getMetadata());
            variation.setMetadata(metadataJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert metadata to JSON", e);
        }

        return variation;
    }

    private boolean ifCategoryAndMetaFieldValuesNotExist(Product product, Map<String, String> metadata ) {
        Category category = product.getCategory();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            CategoryMetadataField metaField = categoryMetadataFieldRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Meta field does not exist: " + name));

            Optional<CategoryMetadataFieldValues> metaValuesOpt =
                    categoryMetadataFieldValuesRepository.findByCategoryAndMetadataField(category, metaField);

            if (metaValuesOpt.isEmpty() || valueNotExist(Arrays.asList(metaValuesOpt.get().getValue().split(",")), value)) {
                return true;
            }
        }
        return false;
    }

    private boolean valueNotExist(List<String> list, String value) {
        return list.stream().noneMatch(val -> val.equalsIgnoreCase(value));
    }

    private boolean ifStructureIsNotSame(ObjectMapper mapper, Product product, Set<String> incomingKeys) {
        List<ProductVariation> variations = productVariationRepository.findByProductId(product.getId());

        if (!variations.isEmpty()) {
            try {
                Set<String> existingKeys = mapper.readValue(
                        variations.get(0).getMetadata(), new TypeReference<Map<String, String>>() {
                        }).keySet();
                return !existingKeys.equals(incomingKeys);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse existing variation metadata", e);
            }
        }
        return false;
    }

    private boolean ifVariationIsSame(ObjectMapper mapper, Product product, Map<String,String> currentMetadata) {
        List<ProductVariation> variations = productVariationRepository.findByProductId(product.getId());
        for (ProductVariation existing : variations) {
            try {
                Map<String, String> existingMetadata =
                        mapper.readValue(existing.getMetadata(), new TypeReference<>() {
                        });
                if (existingMetadata.equals(currentMetadata)) {
                    return true;
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse existing variation metadata", e);
            }
        }
        return false;
    }

    private boolean ifImageNotInCorrectFormat(MultipartFile image) {
        String filename = image.getOriginalFilename();
        return !filename.matches(".*\\.(png|jpg|jpeg|bmp)$");
    }


    public ResponseEntity<?> viewProductVariation(UUID variationId, Seller seller) {
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variation not found"));

        Product product = variation.getProduct();

        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Product has been deleted");
        }

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorisedAccessException("Unauthorized access to the variation");
        }

        ProductVariationResponseVO responseDTO = new ProductVariationResponseVO();
        responseDTO.setId(variation.getId());
        responseDTO.setPrice(variation.getPrice());
        responseDTO.setQuantityAvailable(variation.getQuantityAvailable());
        responseDTO.setMetadata(variation.getMetadata());
        String imageUrl = "http://localhost:8080/users/product-variation/" + variation.getPrimaryImageName();
        responseDTO.setPrimaryImage(imageUrl);
//        responseDTO.setPrimaryImage(variation.getPrimaryImageName());

        // Optionally include parent product details
        responseDTO.setProductName(product.getName());
        responseDTO.setProductDescription(product.getDescription());

        return ResponseEntity.ok(responseDTO);
    }


    public ResponseEntity<Object> viewProductVariation(UUID variationId, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!(user instanceof Seller seller)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only sellers can view product variations");
        }

        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variation not found"));

        Product product = variation.getProduct();

        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Product has been deleted");
        }

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorisedAccessException("Unauthorized access to the variation");
        }

        ProductVariationResponseVO responseDTO = buildProductVariationResponse(variation, product);

        return ResponseEntity.ok(responseDTO);
    }

    private ProductVariationResponseVO buildProductVariationResponse(ProductVariation variation, Product product) {
        ProductVariationResponseVO responseDTO = new ProductVariationResponseVO();
        responseDTO.setId(variation.getId());
        responseDTO.setPrice(variation.getPrice());
        responseDTO.setQuantityAvailable(variation.getQuantityAvailable());
        responseDTO.setMetadata(variation.getMetadata());
        responseDTO.setPrimaryImage(variation.getPrimaryImageName());
        responseDTO.setProductName(product.getName());
        responseDTO.setProductDescription(product.getDescription());
        responseDTO.setProductId(product.getId());
        responseDTO.setIsActive(product.getIsActive());
        return responseDTO;
    }


    public ResponseEntity<?> getAllVariationsByProduct(
            String email, UUID productId,
            Integer max, Integer offset,
            String sort, String order,
            String query
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!(user instanceof Seller seller)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only sellers can access this resource.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));

        if (!product.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You are not the owner of this product.");
        }

        if (product.isDeleted()) {
            return ResponseEntity.badRequest().body("Product is deleted.");
        }

        int page = offset != null ? offset : 0;
        int size = max != null ? max : 10;

        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (sort != null && !sort.isBlank()) ? sort : "price";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<ProductVariation> specification = (root, query1, cb) -> {
            Predicate productPredicate = cb.equal(root.get("product").get("id"), productId);

            if (query != null && !query.isBlank()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("product").get("name")), "%" + query.toLowerCase() + "%");
                return cb.and(productPredicate, namePredicate);
            } else {
                return productPredicate;
            }
        };

        Page<ProductVariation> variationPage = productVariationRepository.findAll(specification, pageable);

        List<ProductVariationResponseVO> responseList = variationPage.stream().map(variation -> {
            ProductVariationResponseVO vo = new ProductVariationResponseVO();
            vo.setId(variation.getId());
            vo.setPrice(variation.getPrice());
            vo.setQuantityAvailable(variation.getQuantityAvailable());
            vo.setPrimaryImage(variation.getPrimaryImageName());
            vo.setMetadata(variation.getMetadata());
            vo.setIsActive(product.getIsActive());
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setProductDescription(product.getDescription());
            return vo;
        }).toList();

        return ResponseEntity.ok(responseList);
    }


    public ResponseEntity<String> updateProductVariation(UUID variationId, ProductVariationUpdateRequestCO dto, Seller seller) {
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));

        Product product = variation.getProduct();

        // Check ownership
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorisedAccessException("You are not the owner of this product variation");
        }

        // Check product status
        if (!product.getIsActive() || product.isDeleted()) {
            throw new RuntimeException("Product is not active or has been deleted");
        }

        ObjectMapper mapper = new ObjectMapper();

        // Metadata validation and update
        if (dto.getMetadata() != null && !dto.getMetadata().isEmpty()) {
            if (ifCategoryAndMetaFieldValuesNotExist(product, dto.getMetadata())) {
                throw new RuntimeException("Invalid metadata values for the category");
            }

            if (ifStructureIsNotSame(mapper, product, dto.getMetadata().keySet())) {
                throw new RuntimeException("Metadata structure does not match");
            }

            if (ifVariationIsSame(mapper, product, dto.getMetadata())) {
                throw new DuplicateResourceException("An identical variation already exists");
            }

            try {
                String metadataJson = mapper.writeValueAsString(dto.getMetadata());
                variation.setMetadata(metadataJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Metadata conversion error", e);
            }
        }

        // Update optional fields
        if (dto.getQuantityAvailable() != null) {
            variation.setQuantityAvailable(dto.getQuantityAvailable());
        }

        if (dto.getPrice() != null) {
            variation.setPrice(dto.getPrice());
        }

        if (dto.getIsActive() != null) {
            variation.setIsActive(dto.getIsActive());
        }

        // Handle primary image update
        if (dto.getPrimaryImage() != null && !dto.getPrimaryImage().isEmpty()) {
            if (ifImageNotInCorrectFormat(dto.getPrimaryImage())) {
                throw new RuntimeException("Invalid image format (must be png/jpg/jpeg/bmp)");
            }

            try {
                String image = fileStorageUtil.saveImage(dto.getPrimaryImage(), product.getId());
                variation.setPrimaryImageName(image);
            } catch (IOException e) {
                throw new RuntimeException("Error saving image", e);
            }
        }

        productVariationRepository.save(variation);
        return ResponseEntity.ok("Product variation updated successfully");
    }




    // customer

    public ProductDetailForCustomerVO viewProductForCustomer(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.isDeleted() || !product.getIsActive()) {
            throw new RuntimeException("Product is either deleted or inactive");
        }

        List<ProductVariation> variations = productVariationRepository.findByProductId(productId);
        List<ProductVariationDetailsForCustomerVO> variationDetails = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (ProductVariation variation : variations) {
            String metadataString;
            try {
                // First, parse to validate it's proper JSON
                Map<String, String> metadataMap = mapper.readValue(variation.getMetadata(), new TypeReference<>() {});
                // Convert it back to a clean JSON string
                metadataString = mapper.writeValueAsString(metadataMap);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse metadata", e);
            }

            String imageUrl = "http://localhost:8080/images/" + variation.getPrimaryImageName();

            variationDetails.add(new ProductVariationDetailsForCustomerVO(
                    variation.getId(),
                    variation.getPrice(),
                    variation.getQuantityAvailable(),
                    metadataString,
                    imageUrl
            ));
        }

        // Build category hierarchy
        Category category = product.getCategory();
        List<String> parentNames = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentNames.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentNames);

        CategoryDetailsVO categoryVO = new CategoryDetailsVO(
                category.getId(),
                category.getCategoryName(),
                parentNames
        );

        return new ProductDetailForCustomerVO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                categoryVO,
                variationDetails
        );
    }
    public ProductDetailForCustomerVO viewProductsForAdmin(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

//        if (product.isDeleted() || !product.getIsActive()) {
//            throw new RuntimeException("Product is either deleted or inactive");
//        }

        List<ProductVariation> variations = productVariationRepository.findByProductId(productId);
        List<ProductVariationDetailsForCustomerVO> variationDetails = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (ProductVariation variation : variations) {
            String metadataString;
            try {
                // First, parse to validate it's proper JSON
                Map<String, String> metadataMap = mapper.readValue(variation.getMetadata(), new TypeReference<>() {});
                // Convert it back to a clean JSON string
                metadataString = mapper.writeValueAsString(metadataMap);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse metadata", e);
            }

            String imageUrl = "http://localhost:8080/images/" + variation.getPrimaryImageName();

            variationDetails.add(new ProductVariationDetailsForCustomerVO(
                    variation.getId(),
                    variation.getPrice(),
                    variation.getQuantityAvailable(),
                    metadataString,
                    imageUrl
            ));
        }

        // Build category hierarchy
        Category category = product.getCategory();
        List<String> parentNames = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentNames.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentNames);

        CategoryDetailsVO categoryVO = new CategoryDetailsVO(
                category.getId(),
                category.getCategoryName(),
                parentNames
        );

        return new ProductDetailForCustomerVO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                categoryVO,
                variationDetails
        );
    }




    public Page<ProductDetailForCustomerVO> getProductsByCategoryForCustomer(UUID categoryId, PaginationRequestVO requestVO) {
        Pageable pageable = PageRequest.of(
                requestVO.getOffset(),
                requestVO.getMax(),
                Sort.by(Sort.Direction.fromString(requestVO.getOrder()), requestVO.getSort())
        );

        // Get list of category IDs (including children if not a leaf)
        List<UUID> categoryIds = new ArrayList<>();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Category> allChildren = findAllChildrenRecursively(category.getId());
        if (allChildren.isEmpty()) {
            categoryIds.add(category.getId());
        } else {
            categoryIds.addAll(allChildren.stream().map(Category::getId).toList());
        }

        //  Build Specification
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(root.get("category").get("id").in(categoryIds));

            // Add query filters (dynamic fields)
            requestVO.getQuery().forEach((key, value) ->
                    predicates.add(cb.equal(root.get(key), value)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        //  Fetch products
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Convert to VO
        List<ProductDetailForCustomerVO> productDetails = productPage.stream()
                .map(product -> viewProductForCustomer(product.getId())) // Your existing helper method
                .toList();

        return new PageImpl<>(productDetails, pageable, productPage.getTotalElements());
    }


    public List<Category> findAllChildrenRecursively(UUID parentId) {
        List<Category> result = new ArrayList<>();
        Queue<UUID> queue = new LinkedList<>();
        queue.add(parentId);

        while (!queue.isEmpty()) {
            UUID currentId = queue.poll();
            List<Category> children = categoryRepository.findByParentCategory_Id(currentId);
            result.addAll(children);
            for (Category child : children) {
                queue.add(child.getId());
            }
        }

        return result;
    }

    public Page<ProductDetailForCustomerVO> getSimilarProductsByBrand(UUID productId, PaginationRequestVO requestVO) {
        Product baseProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        String brand = baseProduct.getBrand();
        Category category = baseProduct.getCategory();

        Pageable pageable = PageRequest.of(
                requestVO.getOffset(),
                requestVO.getMax(),
                Sort.by(Sort.Direction.fromString(requestVO.getOrder()), requestVO.getSort())
        );

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.notEqual(root.get("id"), productId)); // Exclude base product
            predicates.add(cb.equal(root.get("category"), category));

            // Optional additional filters
            requestVO.getQuery().forEach((key, value) -> {
                predicates.add(cb.equal(root.get(key), value));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDetailForCustomerVO> similarProducts = productPage.stream()
                .map(this::buildProductDetailForCustomerVO)
                .toList();

        return new PageImpl<>(similarProducts, pageable, productPage.getTotalElements());
    }

    private ProductDetailForCustomerVO buildProductDetailForCustomerVO(Product product) {
        // Fetch variations manually because Product entity does not have variations
        List<ProductVariation> variations = productVariationRepository.findByProductId(product.getId());

        List<ProductVariationDetailsForCustomerVO> variationDetails = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (ProductVariation variation : variations) {
            String metadataString;
            try {
                Map<String, String> metadataMap = mapper.readValue(variation.getMetadata(), new TypeReference<>() {});
                metadataString = mapper.writeValueAsString(metadataMap);
            } catch (JsonProcessingException e) {
                metadataString = "{}"; // fallback
            }

            String imageUrl = "http://localhost:8080/images/" + variation.getPrimaryImageName();

            variationDetails.add(new ProductVariationDetailsForCustomerVO(
                    variation.getId(),
                    variation.getPrice(),
                    variation.getQuantityAvailable(),
                    metadataString,
                    imageUrl
            ));
        }

        // Build category hierarchy
        Category category = product.getCategory();
        List<String> parentNames = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentNames.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentNames);

        CategoryDetailsVO categoryVO = new CategoryDetailsVO(
                category.getId(),
                category.getCategoryName(),
                parentNames
        );

        return new ProductDetailForCustomerVO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                categoryVO,
                variationDetails
        );
    }





    //admin
    public String deactivateProduct(UUID productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is already inactive");
        }

        product.setIsActive(false);
        productRepository.save(product);
        emailService.sendDeactivationEmail(product.getSeller().getEmail(), product.getName());
        return "Product deactivated and email notification sent.";
    }

    public void activateProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getIsActive()) {
            throw new RuntimeException("Product is already active");
        }

        product.setIsActive(true);
        productRepository.save(product);

        emailService.sendProductActivationEmail(product.getSeller().getEmail(), product.getName());
    }
    public ProductDetailForCustomerVO viewProductForAdmin(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));



        List<ProductVariation> variations = productVariationRepository.findByProductId(productId);
        List<ProductVariationDetailsForCustomerVO> variationDetails = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (ProductVariation variation : variations) {
            String metadataString;
            try {
                // First, parse to validate it's proper JSON
                Map<String, String> metadataMap = mapper.readValue(variation.getMetadata(), new TypeReference<>() {});
                // Convert it back to a clean JSON string
                metadataString = mapper.writeValueAsString(metadataMap);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse metadata", e);
            }

            String imageUrl = "http://localhost:8080/images/" + variation.getPrimaryImageName();

            variationDetails.add(new ProductVariationDetailsForCustomerVO(
                    variation.getId(),
                    variation.getPrice(),
                    variation.getQuantityAvailable(),
                    metadataString,
                    imageUrl
            ));
        }

        // Build category hierarchy
        Category category = product.getCategory();
        List<String> parentNames = new ArrayList<>();
        Category parent = category.getParentCategory();
        while (parent != null) {
            parentNames.add(parent.getCategoryName());
            parent = parent.getParentCategory();
        }
        Collections.reverse(parentNames);

        CategoryDetailsVO categoryVO = new CategoryDetailsVO(
                category.getId(),
                category.getCategoryName(),
                parentNames
        );

        return new ProductDetailForCustomerVO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                categoryVO,
                variationDetails
        );
    }


    public Page<ProductDetailForCustomerVO> getAllProductsForAdmin(PaginationRequestVO requestVO) {
        Pageable pageable = PageRequest.of(
                requestVO.getOffset(),
                requestVO.getMax(),
                Sort.by(Sort.Direction.fromString(requestVO.getOrder()), requestVO.getSort())
        );

        Page<Product> productPage = productRepository.findAll(pageable); 

        List<ProductDetailForCustomerVO> productDetails = productPage.stream()
                .map(product -> viewProductsForAdmin(product.getId()))
                .toList();

        return new PageImpl<>(productDetails, pageable, productPage.getTotalElements());
    }


}


