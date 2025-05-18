package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    Optional<Address> findByIdAndUserEmail(UUID addressId, String userEmail);
}
