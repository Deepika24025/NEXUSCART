package com.ttn.nexuscart.entity.category;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "category_metadata_field_values")
@Getter
@Setter
@NoArgsConstructor
public class CategoryMetadataFieldValues {

    @EmbeddedId
    private CategoryMetadataFieldValuesId id;

    @ManyToOne
    @MapsId("metadataFieldId")
    @JsonManagedReference
    @JoinColumn(name = "category_metadata_field_id", nullable = false)
    private CategoryMetadataField metadataField;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "metadata_value", nullable = false)
    private String value;


}
