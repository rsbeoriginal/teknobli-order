package com.tecknobli.order.dto;

import java.util.List;

public class UserCartDTO {

    private String userId;
    private List<ProductDTO> products;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "UserCartDTO{" +
                "userId='" + userId + '\'' +
                ", products=" + products +
                '}';
    }
}
