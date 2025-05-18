package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndRolesAuthority(UUID id,String role);
    boolean existsByEmail(String email);

}
