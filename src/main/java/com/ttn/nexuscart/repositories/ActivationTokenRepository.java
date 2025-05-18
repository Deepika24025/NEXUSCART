package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.ActivationToken;
import com.ttn.nexuscart.entity.users.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    ActivationToken findByToken(String token);

    ActivationToken findByUser(Customer customer);
}
