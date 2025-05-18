package com.ttn.nexuscart.entity.users;

import com.ttn.nexuscart.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "seller")
@PrimaryKeyJoinColumn(name = "id")
public class Seller extends User {
    private String gst;
    private String companyName;
    private String companyContact;


    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();


}
