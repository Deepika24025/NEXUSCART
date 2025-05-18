package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.co.AddressCO;
import com.ttn.nexuscart.co.SellerCO;
import com.ttn.nexuscart.co.UpdateSellerProfileRequestCO;
import com.ttn.nexuscart.vo.SellerProfileResponseVO;
import com.ttn.nexuscart.service.AddressService;
import com.ttn.nexuscart.service.SellerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seller")
public class SellerController {

    private static final Logger log = LoggerFactory.getLogger(SellerController.class);
    @Autowired
    private SellerService sellerService;
    @Autowired
    private AddressService addressService;

    @PostMapping("/register-seller")
    public ResponseEntity<?> registerSeller(@Valid @RequestBody SellerCO sellerCO) {
        log.info("Registering new seller with email: {}", sellerCO.getEmail());
        String message = sellerService.registerSeller(sellerCO);
        log.info("Seller registration completed for email: {}", sellerCO.getEmail());
        return ResponseEntity.ok(message);
    }


    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/view-profile")
    public ResponseEntity<SellerProfileResponseVO> getProfile(Authentication authentication) {
        log.info("Fetching profile for seller: {}", authentication.getName());
        SellerProfileResponseVO profile = sellerService.getProfile(authentication);
        log.debug("Seller profile fetched: {}", profile);
        return ResponseEntity.ok(sellerService.getProfile(authentication));
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @Valid @RequestBody UpdateSellerProfileRequestCO request,
            Authentication authentication) {
        log.info("Updating profile for seller: {}", authentication.getName());
        sellerService.updateProfile(request, authentication);
        log.info("Profile updated for seller: {}", authentication.getName());
        return ResponseEntity.ok("Profile updated successfully.");
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PatchMapping("/update-address/{id}")
    public ResponseEntity<String> updateAddress(
            @PathVariable("id") UUID addressId,
            @Valid @RequestBody AddressCO requestDTO,
            Authentication authentication) {
        log.info("Updating address with ID: {} for seller: {}", addressId, authentication.getName());
        addressService.updateAddress(addressId, requestDTO, authentication.getName());
        log.info("Address updated for ID: {} by seller: {}", addressId, authentication.getName());
        return ResponseEntity.ok("Address updated successfully");
    }

}