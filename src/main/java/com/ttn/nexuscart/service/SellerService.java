package com.ttn.nexuscart.service;

import com.ttn.nexuscart.co.SellerCO;
import com.ttn.nexuscart.co.UpdatePasswordRequestCO;
import com.ttn.nexuscart.co.UpdateSellerProfileRequestCO;
import com.ttn.nexuscart.vo.AddressVO;
import com.ttn.nexuscart.vo.SellerProfileResponseVO;
import com.ttn.nexuscart.entity.Address;
import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.entity.users.Seller;
import com.ttn.nexuscart.exceptions.InvalidInputException;
import com.ttn.nexuscart.exceptions.UserNotFoundException;
import com.ttn.nexuscart.repositories.RoleRepository;
import com.ttn.nexuscart.repositories.SellerRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashSet;
import java.util.Set;


@Service
public class SellerService {
    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    public String registerSeller(SellerCO sellerCO) {
        logger.info("Attempting to register seller with email: {}", sellerCO.getEmail());

        // Check for existing email
        if (sellerRepository.findByEmail(sellerCO.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already exists", sellerCO.getEmail());
            throw new InvalidInputException("Seller with same email already exists");
        }
        // check if any other user exits with the same email
        if (userRepository.existsByEmail(sellerCO.getEmail())) {
            logger.warn("Email {} is already in use", sellerCO.getEmail());
            throw new InvalidInputException("Email already registered with another account.");
        }

        // Check for existing company name
        if (sellerRepository.findByCompanyNameIgnoreCase(sellerCO.getCompanyName()).isPresent()) {
            logger.warn("Registration failed: Company name {} already exists", sellerCO.getCompanyName());
            throw new InvalidInputException("Seller with same company name already exists");
        }

        //  Check for existing GST number
        if (sellerRepository.existsByGst(sellerCO.getGst())) {
            logger.warn("Registration failed: GST number {} already exists", sellerCO.getGst());
            throw new InvalidInputException("Seller with same GST number already exists");
        }

        if (!sellerCO.getPassword().equals(sellerCO.getConfirmPassword())) {
            logger.warn("Password and confirm password do not match for email: {}", sellerCO.getEmail());
            throw new InvalidInputException("Password and ConfirmPassword doesn't match");
        }

        Role role = roleRepository.findByAuthority("SELLER");
        Seller seller = new Seller();
        seller.setEmail(sellerCO.getEmail());
        seller.setPassword(passwordEncoder.encode(sellerCO.getPassword()));
        seller.setGst(sellerCO.getGst());
        seller.setCompanyName(sellerCO.getCompanyName());
        seller.setCompanyContact(sellerCO.getCompanyContact());
        seller.setFirstName(sellerCO.getFirstName());
        seller.setLastName(sellerCO.getLastName());
        seller.setIsActive(true);

        Set<Address> address = sellerCO.getAddress();
        for (Address sellerAddress : address) {
            sellerAddress.setUser(seller);
        }
        seller.setAddresses(address);

        // Setting  roles
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        seller.setRoles(roles);

        sellerRepository.save(seller);
        logger.info("Seller registered successfully: {}", seller.getEmail());

        emailService.sendAsyncMail(seller.getEmail());
        logger.info("Registration email sent to: {}", seller.getEmail());

        return "Seller registered successfully";
    }

    public SellerProfileResponseVO getProfile(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Fetching profile for seller: {}", email);


        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Seller not found for email: {}", email);
                    return new UserNotFoundException("Seller not found");
                });

        SellerProfileResponseVO response = new SellerProfileResponseVO();
        response.setEmail(seller.getEmail());
        response.setFirstName(seller.getFirstName());
        response.setLastName(seller.getLastName());
        response.setIsActive(seller.getIsActive());
        response.setCompanyContact(seller.getCompanyContact());
        response.setCompanyName(seller.getCompanyName());
        response.setGst(seller.getGst());

        if (seller.getImage() != null) {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/users/images/")
                    .path(seller.getImage())
                    .toUriString();
            response.setImage(imageUrl);
        }

        Set<Address> addresses = seller.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.iterator().next();
            AddressVO addressVO = new AddressVO();
            addressVO.setCity(address.getCity());
            addressVO.setState(address.getState());
            addressVO.setCountry(address.getCountry());
            addressVO.setAddressLine(address.getAddressLine());
            addressVO.setZipCode(address.getZipCode());
            response.setAddress(addressVO);
        }
        logger.info("Profile fetched successfully for seller: {}", email);
        return response;
    }

    public void updateProfile(UpdateSellerProfileRequestCO request, Authentication authentication) {
        String email = authentication.getName();
        logger.info("Updating profile for seller: {}", email);

        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Seller not found during profile update: {}", email);
                    return new UserNotFoundException("Seller not found");
                });

        if (request.getFirstName() != null)
            seller.setFirstName(request.getFirstName());

        if (request.getLastName() != null)
            seller.setLastName(request.getLastName());

        if (request.getCompanyContact() != null)
            seller.setCompanyContact(request.getCompanyContact());

        if (request.getCompanyName() != null)
            seller.setCompanyName(request.getCompanyName());

        if (request.getGst() != null)
            seller.setGst(request.getGst());

        sellerRepository.save(seller);
        logger.info("Profile updated successfully for seller: {}", email);
    }


    public void updatePassword(UpdatePasswordRequestCO request, Authentication authentication) {
        String email = authentication.getName();
        logger.info("Updating password for seller: {}", email);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password must match");
        }


        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Seller not found during password update: {}", email);
                    return new UserNotFoundException("Seller not found");
                });

        if (passwordEncoder.matches(request.getPassword(), seller.getPassword())) {
            logger.error("New password is same as old password for seller: {}", email);
            throw new IllegalArgumentException("New password cannot be same as the old password!");
        }

        seller.setPassword(passwordEncoder.encode(request.getPassword()));
        sellerRepository.save(seller);

        emailService.sendPasswordChangeConfirmation(email);
        logger.info("Password updated and confirmation email sent for seller: {}", email);
    }

}
