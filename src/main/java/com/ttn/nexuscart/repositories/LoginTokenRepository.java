package com.ttn.nexuscart.repositories;

import com.ttn.nexuscart.entity.Token;
import com.ttn.nexuscart.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginTokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    List<Token> findAllByUser(User user);

    void deleteByUser(User user);

    List<Token> findByUserAndIsDeletedFalse(User user);

    List<Token> findByUser(User user);
}
