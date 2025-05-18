package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.PasswordResetToken;
import com.ttn.nexuscart.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
