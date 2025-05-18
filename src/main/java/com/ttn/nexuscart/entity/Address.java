package com.ttn.nexuscart.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ttn.nexuscart.entity.users.User;
import com.ttn.nexuscart.security.config.AuditMetaData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address extends AuditMetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
    private String label;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    private Boolean isDeleted = false;

}
