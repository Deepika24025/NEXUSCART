package com.ttn.nexuscart.entity.category;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "CATEGORY_METADATA_FIELD")
@Getter
@Setter
public class CategoryMetadataField {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "metadataField", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<CategoryMetadataFieldValues> metadataValues;
}
