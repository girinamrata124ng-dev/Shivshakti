package com.inn.cafe.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductWrapper {
    Integer id;
    String name;
    String description;
    Integer price;
    String status;

    public ProductWrapper(Integer id, String name , String description , Integer price , String status ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
