package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.users.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c JOIN c.roles r " +
            "WHERE r.authority = 'CUSTOMER' " +
            "AND (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Customer> findAllCustomers(@Param("email") String email, Pageable pageable);
}
