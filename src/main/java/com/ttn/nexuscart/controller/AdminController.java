package com.ttn.nexuscart.controller;

import com.ttn.nexuscart.vo.CustomerVO;
import com.ttn.nexuscart.vo.SellerVO;
import com.ttn.nexuscart.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private final MessageSource messageSource;
    @Autowired
    private AdminService adminService;

    public AdminController(MessageSource messageSource) {

        this.messageSource = messageSource;
    }

    @GetMapping("/getallcustomer")
    public ResponseEntity<Page<CustomerVO>> getAllCustomer(@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageOffset, @RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "id") String sortBy) {
        log.info("Fetching all customers with pageSize={}, pageOffset={}, emailFilter={}, sortBy={}", pageSize, pageOffset, email, sortBy);
        return ResponseEntity.ok(adminService.getAllCustomer(pageSize, pageOffset, email, sortBy));
    }

    @GetMapping("/getallseller")
    public ResponseEntity<Page<SellerVO>> getAllSeller(@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageOffset, @RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "id") String sortBy) {
        log.info("Fetching all sellers with pageSize={}, pageOffset={}, emailFilter={}, sortBy={}", pageSize, pageOffset, email, sortBy);
        return ResponseEntity.ok(adminService.getAllSeller(pageSize, pageOffset, email, sortBy));
    }


    @PatchMapping("/activate-customer/{id}")
    public ResponseEntity<String> activateCustomer(@PathVariable UUID id) {
        log.info("Activating customer with ID={}", id);
        String result =adminService.activateCustomerById(id);
        String localizedMessage = messageSource.getMessage(result, null, LocaleContextHolder.getLocale());
        return new ResponseEntity<>(localizedMessage, HttpStatus.OK);
    }

    @PatchMapping("/deactivate-customer/{id}")
    public ResponseEntity<String> deactivateCustomer(@PathVariable UUID id) {
        log.info("Deactivating customer with ID={}", id);
        String result = adminService.deactivateCustomerById(id);
        String localizedMessage = messageSource.getMessage(result, null, LocaleContextHolder.getLocale());
        return new ResponseEntity<>(localizedMessage, HttpStatus.OK);
    }

    @PatchMapping("/activate-seller/{id}")
    public ResponseEntity<String> activateSeller(@PathVariable UUID id) {
        log.info("Activating seller with ID={}", id);
        String result = adminService.activateSellerById(id);
        String localizedMessage = messageSource.getMessage(result, null, LocaleContextHolder.getLocale());
        return new ResponseEntity<>(localizedMessage, HttpStatus.OK);
    }

    @PatchMapping("/deactivate-seller/{id}")
    public ResponseEntity<String> deactivateSeller(@PathVariable UUID id) {
        log.info("Deactivating seller with ID={}", id);
        String result = adminService.deactivateSellerById(id);
        String localizedMessage = messageSource.getMessage(result, null, LocaleContextHolder.getLocale());
        return new ResponseEntity<>(localizedMessage, HttpStatus.OK);
    }


}
