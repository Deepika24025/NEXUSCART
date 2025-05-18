package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.users.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {

    Optional<Seller> findByEmail(String email);

    Optional<Seller> findByCompanyNameIgnoreCase(String companyName);

    @Query("SELECT s FROM Seller s LEFT JOIN s.roles r " +
            "WHERE (r.authority = 'SELLER' OR r IS NULL) " +
            "AND (:email IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Seller> findAllSellers(@Param("email") String email, Pageable pageable);

    boolean existsByGst(String gst);

    Optional<Seller> findByEmailIgnoreCase(String email);
}
