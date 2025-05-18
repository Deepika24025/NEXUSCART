package com.ttn.nexuscart.entity.users;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ttn.nexuscart.entity.Address;
import com.ttn.nexuscart.entity.Role;
import com.ttn.nexuscart.security.config.AuditMetaData;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends AuditMetaData {
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Address> addresses = new HashSet<>();
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;
    @Column(name = "FIRST_NAME")
    @NotBlank
    private String firstName;
    @Column(name = "MIDDLE_NAME")
    private String middleName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "PASSWORD")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @Column(name = "IS_DELETED")
    private Boolean isDeleted = false;
    @Column(name = "IS_ACTIVE")
    private Boolean isActive = false;
    @Column(name = "IS_EXPIRED")
    private Boolean isExpired = false;
    @Column(name = "IS_LOCKED")
    private Boolean isLocked = false;
    @Column(name = "INVALID_ATTEMPT_COUNT")
    private int invalidAttemptCount;
    @Column(name = "PASSWORD_UPDATE_DATE")
    private LocalDateTime passwordUpdateDate;
    @ManyToMany
    @JoinTable(
            name = "USER_ROLE",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    private Set<Role> roles = new HashSet<>();

    private String image;


//    @Column(name = "password_updated_at")
//    private LocalDateTime passwordUpdatedAt;
//
//    @Transient
//    private final int PASSWORD_EXPIRY_DAYS = 90;
//
//    public boolean isPasswordExpired() {
//        if (passwordUpdatedAt == null) return true; // consider expired if never set
//        return passwordUpdatedAt.plusDays(PASSWORD_EXPIRY_DAYS).isBefore(LocalDateTime.now());
//    }

}
