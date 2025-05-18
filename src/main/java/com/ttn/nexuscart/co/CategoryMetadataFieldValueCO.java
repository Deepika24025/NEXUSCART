package com.ttn.nexuscart.co;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryMetadataFieldValueCO {
    private String fieldName;
    private List<String> values;
}
