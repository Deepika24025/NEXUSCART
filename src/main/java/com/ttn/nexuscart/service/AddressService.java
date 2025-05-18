package com.ttn.nexuscart.service;

import com.ttn.nexuscart.co.AddressCO;
import com.ttn.nexuscart.entity.Address;
import com.ttn.nexuscart.entity.users.Customer;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.exceptions.UnauthorisedAccessException;
import com.ttn.nexuscart.exceptions.UserNotFoundException;
import com.ttn.nexuscart.repositories.AddressRepository;
import com.ttn.nexuscart.repositories.CustomerRepository;
import com.ttn.nexuscart.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AddressService {
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public void updateAddress(UUID addressId, AddressCO dto, String email) {
        log.info("Attempting to update address with ID: {} by user: {}", addressId, email);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.error("Address not found with id: {}", addressId);
                    return new IllegalArgumentException("Address not found with id: " + addressId);
                });


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                            log.error("User not found with email: {}", email);
                            return new UserNotFoundException("User not found");
                        }
                );

        if (!address.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized address update attempt by user: {}", email);
            throw new UnauthorisedAccessException("You are not authorized to update this address");
        }

        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setAddressLine(dto.getAddressLine());
        address.setZipCode(dto.getZipCode());

        addressRepository.save(address);
        log.info("Address updated successfully for ID: {}", addressId);
    }


    public List<AddressCO> getAddressesForCustomer(String email) {
        log.info("Fetching addresses for customer with email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", email);
                    return new UserNotFoundException("Customer not found");
                });

        Set<Address> addressSet = customer.getAddresses();

        return addressSet.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AddressCO convertToDTO(Address address) {
        AddressCO dto = new AddressCO();
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setAddressLine(address.getAddressLine());
        dto.setLabel(address.getLabel());
        dto.setZipCode(address.getZipCode());
        return dto;
    }


    public void addAddressForCustomer(AddressCO request, String email) {
        // Fetch customer by username
        log.info("Adding new address for customer with email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Customer not found for email: {}", email);
                    return new UserNotFoundException("Customer not found with email: " + email);
                });

        Set<Address> addresses = customer.getAddresses();

        Address address = new Address();
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setLabel(request.getLabel());
        address.setZipCode(request.getZipCode());
        address.setUser(customer);

//        addressRepository.save(address);

        addresses.add(address);

//        addressRepository.save(address);
        customerRepository.save(customer);
        log.info("Address added successfully for customer: {}", email);
    }


    public void deleteAddressForCustomer(UUID addressId, String userEmail) {
        log.info("Attempting to delete address with ID: {} for user: {}", addressId, userEmail);
        Address address = addressRepository.findByIdAndUserEmail(addressId, userEmail)
                .orElseThrow(() -> {
                    log.warn("Access denied or address not found for ID: {} and user: {}", addressId, userEmail);
                    return new AccessDeniedException("Address not found or access denied.");
                });

        address.setIsDeleted(true);
        addressRepository.save(address);
        log.info("Address marked as deleted for ID: {} by user: {}", addressId, userEmail);
    }
}
