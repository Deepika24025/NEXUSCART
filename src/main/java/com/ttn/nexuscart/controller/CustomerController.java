package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.co.AddressCO;
import com.ttn.nexuscart.co.CustomerCO;
import com.ttn.nexuscart.co.UpdateCustomerProfileRequestCO;
import com.ttn.nexuscart.vo.CustomerProfileResponseVO;
import com.ttn.nexuscart.entity.Address;
import com.ttn.nexuscart.service.AddressService;
import com.ttn.nexuscart.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RequestMapping("/customer")
@RestController
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    @Autowired
    private final CustomerService customerService;
    Address address;
    @Autowired
    private AddressService addressService;


    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register-customer")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody CustomerCO customerCO) {
        log.info("Registering User: {}", customerCO.getEmail());
        Map<String, String> response = customerService.registerUser(customerCO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam String token) {
        return customerService.activateUser(token);
    }

    @PostMapping("/resend-verification-link")
    public ResponseEntity<String> resendVerificationLink(@RequestParam String email) {
        log.info("Resending verification link to email: {}", email);
        return customerService.resendVerificationLink(email);
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/customer-profile")
    public ResponseEntity<CustomerProfileResponseVO> viewProfile(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching profile for customer: {}", email);
        CustomerProfileResponseVO profile = customerService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/customer-addresses")
    public ResponseEntity<List<AddressCO>> getCustomerAddresses(Authentication authentication) {
        String email = authentication.getName();
        log.info("Getting addresses for customer: {}", email);
        List<AddressCO> addresses = addressService.getAddressesForCustomer(authentication.getName());
        return ResponseEntity.ok(addresses);
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @Valid @RequestBody UpdateCustomerProfileRequestCO request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Updating profile for customer: {}", email);
        customerService.updateProfile(request, authentication);
        return ResponseEntity.ok("Profile updated successfully.");
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/add-address")
    public ResponseEntity<String> addNewAddresses(@RequestBody AddressCO request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Adding new address for customer: {}", email);
        addressService.addAddressForCustomer(request, authentication.getName());
        return ResponseEntity.ok(" New Address added successfully ");
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @DeleteMapping("/delete-address/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable UUID addressId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting address with ID: {} for customer: {}", addressId, email);
        addressService.deleteAddressForCustomer(addressId, authentication.getName());
        return ResponseEntity.ok("Address deleted successfully");
    }


    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/update-address/{id}")
    public ResponseEntity<String> updateAddress(
            @PathVariable("id") UUID addressId,
            @Valid @RequestBody AddressCO requestDTO,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Updating address with ID: {} for customer: {}", addressId, email);
        addressService.updateAddress(addressId, requestDTO, authentication.getName());
        if (address.getIsDeleted()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Address dont exist");
        }
        return ResponseEntity.ok("Address updated successfully");
    }

}

