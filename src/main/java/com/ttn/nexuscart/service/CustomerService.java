package com.ttn.nexuscart.service;

import com.ttn.nexuscart.co.CustomerCO;
import com.ttn.nexuscart.co.UpdateCustomerProfileRequestCO;
import com.ttn.nexuscart.co.UpdatePasswordRequestCO;
import com.ttn.nexuscart.vo.CustomerProfileResponseVO;
import com.ttn.nexuscart.entity.ActivationToken;
import com.ttn.nexuscart.entity.Address;
import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.entity.users.Customer;
import com.ttn.nexuscart.exceptions.DuplicateResourceException;
import com.ttn.nexuscart.exceptions.InvalidInputException;
import com.ttn.nexuscart.exceptions.UserNotFoundException;
import com.ttn.nexuscart.repositories.ActivationTokenRepository;
import com.ttn.nexuscart.repositories.CustomerRepository;
import com.ttn.nexuscart.repositories.RoleRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import com.ttn.nexuscart.security.jwt.JwtService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtService jwtService;
    @Autowired
    ActivationTokenRepository activationTokenRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, RoleRepository roleRepository) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
    }

    public Map<String, String> registerUser(@Valid CustomerCO customerCO) {
        logger.info("Registering new customer with email: {}", customerCO.getEmail());

        Optional<Customer> cs = customerRepository.findByEmail(customerCO.getEmail());
        if (cs.isPresent()) {
            logger.warn("Email {} is already in use", customerCO.getEmail());
            throw new DuplicateResourceException("Email already existing");
        }

        if (userRepository.existsByEmail(customerCO.getEmail())) {
            logger.warn("Email {} is already in use", customerCO.getEmail());
            throw new DuplicateResourceException("Email already registered with another account.");
        }

        if (!customerCO.getPassword().equals(customerCO.getConfirmPassword())) {
            logger.warn("Password and confirm password do not match for email: {}", customerCO.getEmail());
            throw new InvalidInputException("Password and ConfirmPassword doesn't match");
        }

        Role role = roleRepository.findByAuthority("CUSTOMER");

        Customer customer = new Customer();
        customer.setFirstName(customerCO.getFirstName());
        customer.setMiddleName(customerCO.getMiddleName());
        customer.setLastName(customerCO.getLastName());
        customer.setEmail(customerCO.getEmail());
        customer.setPassword(passwordEncoder.encode(customerCO.getPassword()));
        customer.setContact(customerCO.getContact());
//        customer.setPasswordUpdatedAt(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        customer.setRoles(roles);

        Set<Address> addresses = customerCO.getAddresses();
        for (Address address : addresses) {
            address.setUser(customer);
        }
        customer.setAddresses(addresses);

        customerRepository.save(customer);
        logger.info("Customer saved: {}", customer.getEmail());

        String token = jwtService.generateRegistrationToken(customer.getEmail());

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(token);
        activationToken.setUser(customer);
        activationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        activationTokenRepository.save(activationToken);

        logger.info("Activation token created for customer: {}", customer.getEmail());

        String verificationUrl = "http://localhost:8080/customer/verify?token=" + token;
        emailService.sendVerificationEmail(customer.getEmail(), verificationUrl);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!!!");
        response.put("token", token);
        return response;
    }



    public ResponseEntity<?> activateUser(String token) {
        logger.info("Activating user with token: {}", token);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired token!"));
        }

        String email = jwtService.extractEmail(token);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if (customerOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found!"));
        }

        Customer customer = customerOptional.get();
        if (Boolean.TRUE.equals(customer.getIsActive())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Account is already activated."));
        }

        ActivationToken activationToken = activationTokenRepository.findByToken(token);
        if (activationToken == null || activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired token!"));
        }

        customer.setIsActive(true);
        customerRepository.save(customer);
        activationTokenRepository.delete(activationToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User activated successfully!");
        logger.info("Returning activation response: {}", response);// this should now appear in Postman!
        emailService.sendActivationEmailToCustomer(customer.getEmail());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> resendVerificationLink(String email) {
        logger.info("Resending verification link to email: {}", email);
        System.out.println(email);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        System.out.println(customerOptional);
        if (customerOptional.isEmpty()) {
            logger.warn("Resend failed: No user found with email {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User with this email does not exist!");
        }

        Customer customer = customerOptional.get();

        // Checking if user is already activated
        if (customer.getIsActive()) {
            logger.info("Resend skipped: User already active - {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is already activated!");
        }

        // Deletion of existing token (if any)
        ActivationToken existingToken = activationTokenRepository.findByUser(customer);
        if (existingToken != null) {
            activationTokenRepository.delete(existingToken);
        }

        // Generate a new token
        String newToken = jwtService.generateRegistrationToken(customer.getEmail());
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(newToken);
        activationToken.setUser(customer);
        activationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        // Save new token
        activationTokenRepository.save(activationToken);

        // Send email with the new token
        String verificationUrl = "http://localhost:8080/customer/verify?token=" + newToken;
        emailService.sendVerificationEmail(customer.getEmail(), verificationUrl);
        logger.info("New verification link sent to: {}", email);
        return ResponseEntity.ok("New activation link sent to email!");
    }


    public CustomerProfileResponseVO getProfile(String email) {
        logger.info("Fetching profile for customer: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        CustomerProfileResponseVO customerProfileResponseVO = new CustomerProfileResponseVO();
        customerProfileResponseVO.setId(customer.getId());
        customerProfileResponseVO.setFirstName(customer.getFirstName());
        customerProfileResponseVO.setLastName(customer.getLastName());
        customerProfileResponseVO.setContact(customer.getContact());
        customerProfileResponseVO.setActive(customer.getIsActive());
        if (customer.getImage() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/users/images/")
                    .path(customer.getImage())
                    .toUriString();
            customerProfileResponseVO.setImage(imageUrl);
        }

        return customerProfileResponseVO;
    }


    public void updateProfile(UpdateCustomerProfileRequestCO request, Authentication authentication) {
        String email = authentication.getName();
        logger.info("Updating profile for customer: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (request.getFirstName() != null)
            customer.setFirstName(request.getFirstName());

        if (request.getMiddleName() != null)
            customer.setMiddleName(request.getMiddleName());

        if (request.getLastName() != null)
            customer.setLastName(request.getLastName());

        if (request.getContact() != null)
            customer.setContact(request.getContact());

        customerRepository.save(customer);
        logger.info("Profile updated successfully for customer: {}", email);
    }

    public void updatePassword(UpdatePasswordRequestCO request, Authentication authentication) {
        String email = authentication.getName();
        logger.info("Updating password for user: {}", email);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password must match");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Customer not found during password update: {}", email);
                    return new UserNotFoundException("Customer not found");
                });

        if (passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            logger.error("New password is same as old password for seller: {}", email);
            throw new IllegalArgumentException("New password cannot be same as the old password!");
        }

        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customerRepository.save(customer);
        logger.info("Password updated successfully for customer: {}", email);
        emailService.sendPasswordChangeConfirmation(email);
    }
}

