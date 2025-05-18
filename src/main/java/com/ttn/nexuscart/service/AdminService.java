package com.ttn.nexuscart.service;

import com.ttn.nexuscart.vo.CustomerVO;
import com.ttn.nexuscart.vo.SellerVO;
import com.ttn.nexuscart.entity.users.Customer;
import com.ttn.nexuscart.entity.users.Seller;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.repositories.CategoryMetadataFieldRepository;
import com.ttn.nexuscart.repositories.CustomerRepository;
import com.ttn.nexuscart.repositories.SellerRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service

public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    @Autowired
    CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmailService emailService;

    public Page<CustomerVO> getAllCustomer(int pageSize, int pageOffset, String email, String sortBy) {
        log.info("Fetching customers with filter: email='{}', pageOffset={}, pageSize={}, sortBy={}", email, pageOffset, pageSize, sortBy);
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sortBy));
        return customerRepository.findAllCustomers(email, pageable).map(this::mappingToCustomerDTO);
    }

    private CustomerVO mappingToCustomerDTO(Customer customer) {
        CustomerVO customerVO = new CustomerVO();
        customerVO.setId(customer.getId());
        StringBuilder fullNameBuilder = new StringBuilder();
        if (customer.getFirstName() != null) fullNameBuilder.append(customer.getFirstName()).append(" ");
        if (customer.getMiddleName() != null) fullNameBuilder.append(customer.getMiddleName()).append(" ");
        if (customer.getLastName() != null) fullNameBuilder.append(customer.getLastName());
        customerVO.setFullName(fullNameBuilder.toString().trim());
        customerVO.setEmail(customer.getEmail());
        customerVO.setIsActive(customer.getIsActive());

        return customerVO;
    }


    public Page<SellerVO> getAllSeller(int pageSize, int pageOffset, String email, String sortBy) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sortBy));
        if (email == null || email.trim().isEmpty()) {
            return sellerRepository.findAll(pageable).map(this::mappingToSellerDTO);
        }
        return sellerRepository.findAllSellers(email, pageable).map(this::mappingToSellerDTO);

    }

    private SellerVO mappingToSellerDTO(Seller seller) {
        SellerVO sellerDTO = new SellerVO();
        sellerDTO.setId(seller.getId());
        StringBuilder fullNameBuilder = new StringBuilder();
        if (seller.getFirstName() != null) fullNameBuilder.append(seller.getFirstName()).append(" ");
        if (seller.getLastName() != null) fullNameBuilder.append(seller.getLastName());
        sellerDTO.setFullName(fullNameBuilder.toString().trim());
        sellerDTO.setEmail(seller.getEmail());
        sellerDTO.setAddress(seller.getAddresses());
        sellerDTO.setCompanyName(seller.getCompanyName());
        sellerDTO.setCompanyContact(seller.getCompanyContact());
        return sellerDTO;

    }

    public String activateCustomerById(UUID id) {
        log.info("Attempting to activate customer with ID: {}", id);
        User user = userRepository.findByIdAndRolesAuthority(id,"CUSTOMER").orElseThrow(()->new EntityNotFoundException("Customer user not found"));
        if(Boolean.TRUE.equals(user.getIsActive())){
            return "customer.already.activated";
        }
        user.setIsActive(true);
        userRepository.save(user);
        emailService.sendActivationEmail(user.getEmail());
        return "customer.activated";

    }


    public String deactivateCustomerById(UUID id) {
        log.info("Attempting to deactivate customer with ID: {}", id);
        User user = userRepository.findByIdAndRolesAuthority(id,"CUSTOMER").orElseThrow(()->new EntityNotFoundException("Customer user not found"));
        if(Boolean.FALSE.equals(user.getIsActive())){
            return "customer.already.deactivated";
        }
        user.setIsActive(false);
        userRepository.save(user);
        emailService.sendDeActivationEmail(user.getEmail());
        return "customer.deactivated";

    }

    public String activateSellerById(UUID id) {
        log.info("Attempting to activate seller with ID: {}", id);
        User user = userRepository.findByIdAndRolesAuthority(id,"SELLER").orElseThrow(()->new EntityNotFoundException("Customer user not found"));
        if(Boolean.TRUE.equals(user.getIsActive())){
            return "seller.already.activated";
        }
        user.setIsActive(true);
        userRepository.save(user);
        emailService.sendActivationEmail(user.getEmail());
        return "seller.activated";

    }


    public String deactivateSellerById(UUID id) {
        log.info("Attempting to deactivate seller with ID: {}", id);
        User user = userRepository.findByIdAndRolesAuthority(id, "SELLER").orElseThrow(() -> new EntityNotFoundException("Customer user not found"));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return "seller.already.deactivated";
        }
        user.setIsActive(false);
        userRepository.save(user);
        emailService.sendDeActivationEmail(user.getEmail());
        return "seller.deactivated";
    }
}
