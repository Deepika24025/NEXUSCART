package com.ttn.nexuscart.entity;

import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private LocalDateTime expiryDate;

    private boolean isDeleted = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Token() {
    }

    public Token(String token, TokenType tokenType, LocalDateTime expiryDate, User user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiryDate = expiryDate;
        this.user = user;
        this.isDeleted = false;
    }
}
