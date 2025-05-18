package com.ttn.nexuscart.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ttn.nexuscart.co.*;
import com.ttn.nexuscart.vo.PaginationRequestVO;
import com.ttn.nexuscart.vo.ProductDetailForCustomerVO;
import com.ttn.nexuscart.vo.ProductResponseVO;
import com.ttn.nexuscart.entity.users.Seller;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.exceptions.ResourceNotFoundException;
import com.ttn.nexuscart.repositories.SellerRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.CustomUserDetails;
import com.ttn.nexuscart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserRepository userRepository;


    // seller
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @PostMapping("/seller-addproduct")
    public ResponseEntity<?> addProduct(@RequestBody @Valid ProductRequestCO dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {

        Seller seller = sellerRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        productService.addProduct(dto, seller);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Product created and marked as inactive by default");
    }

    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @GetMapping("/seller-viewproduct")
    public ResponseEntity<?> viewProduct(@RequestParam UUID id, Principal principal) {
        ProductResponseVO response = productService.viewProductById(id, principal.getName());
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @GetMapping("/seller-viewallproduct")
    public ResponseEntity<List<ProductResponseVO>> viewAllProducts(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Seller seller = sellerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with email: " + email));

        List<ProductResponseVO> products = productService.viewAllProducts(
                seller, max, offset, sort, order, query
        );

        return ResponseEntity.ok(products);
    }


    @DeleteMapping("/seller-deleteproduct")
    public ResponseEntity<String> deleteProduct(
            @RequestBody RequestIdCO requestIdDto,
            Principal principal
    ) {
        productService.deleteProduct(requestIdDto.getId(), principal.getName());
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @PutMapping("/seller-updateproduct")
    public ResponseEntity<String> updateProduct(
            @RequestBody @Valid UpdateProductRequestCO dto,
            Principal principal
    ) {
        productService.updateProduct(dto, principal.getName());
        return ResponseEntity.ok("Product updated successfully");
    }

    @PostMapping(value = "/variation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductVariation(
            @ModelAttribute @Valid ProductVariationRequestCO dto,
            Authentication authentication
    ) throws JsonProcessingException {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!(user instanceof Seller seller)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only sellers can add product variations");
        }

        return productService.addProductVariation(dto, seller);
    }

    @GetMapping("/view-productvariation")
    public ResponseEntity<?> viewProductVariation(
            @RequestParam UUID variationId,
            Authentication authentication
    ) {
        return productService.viewProductVariation(variationId, authentication);
    }
    @GetMapping("/view-all-productvariations")
    public ResponseEntity<?> getAllVariationsByProduct(
            @RequestBody ProductResponseID productId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Seller seller = sellerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with email: " + email));
        return productService.getAllVariationsByProduct(email, productId.getProductId(), max, offset, sort, order,query);
    }

    @PutMapping("/seller/product-variation/{variationId}")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<?> updateProductVariation(
            @PathVariable UUID variationId,
            @ModelAttribute ProductVariationUpdateRequestCO dto
            ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));

        return productService.updateProductVariation(variationId, dto, seller);
    }



    //customer

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> viewProduct(@PathVariable UUID productId) {
        try {
            ProductDetailForCustomerVO vo = productService.viewProductForCustomer(productId);
            return ResponseEntity.ok(vo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer/products/category/{categoryId}")
    public ResponseEntity<Page<ProductDetailForCustomerVO>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) Map<String, String> query
    ) {

        if (query != null) {
            query.remove("max");
            query.remove("offset");
            query.remove("sort");
            query.remove("order");
        }
        PaginationRequestVO pagination = new PaginationRequestVO();
        pagination.setMax(max);
        pagination.setOffset(offset);
        pagination.setSort(sort);
        pagination.setOrder(order);
        pagination.setQuery(query != null ? query : new HashMap<>());

        Page<ProductDetailForCustomerVO> response = productService.getProductsByCategoryForCustomer(categoryId, pagination);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/products/similar/{productId}")
    public ResponseEntity<Page<ProductDetailForCustomerVO>> getSimilarProducts(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) Map<String, String> query
    ) {

        if (query != null) {
            query.remove("max");
            query.remove("offset");
            query.remove("sort");
            query.remove("order");
        }
        PaginationRequestVO pagination = new PaginationRequestVO();
        pagination.setMax(max);
        pagination.setOffset(offset);
        pagination.setSort(sort);
        pagination.setOrder(order);
        pagination.setQuery(query != null ? query : new HashMap<>());

        Page<ProductDetailForCustomerVO> products = productService.getSimilarProductsByBrand(productId, pagination);
        return ResponseEntity.ok(products);

    }


    //admin

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/product-admin/{productId}")
    public ResponseEntity<?> viewProductForAdmin(@PathVariable UUID productId) {
        try {
            ProductDetailForCustomerVO vo = productService.viewProductForAdmin(productId);
            return ResponseEntity.ok(vo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateProduct(@PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deactivated successfully"));

    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<?> activateProduct(@PathVariable UUID id) {
        productService.activateProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product activated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public ResponseEntity<Page<ProductDetailForCustomerVO>> getAllProductsForAdmin(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) Map<String, String> query
    ) {
        PaginationRequestVO pagination = new PaginationRequestVO();
        pagination.setMax(max);
        pagination.setOffset(offset);
        pagination.setSort(sort);
        pagination.setOrder(order);
        pagination.setQuery(query != null ? query : new HashMap<>());

        Page<ProductDetailForCustomerVO> response = productService.getAllProductsForAdmin(pagination);
        return ResponseEntity.ok(response);
    }


}
