package com.tecknobli.order.dto;

import com.tecknobli.order.entity.UserOrder;

import java.util.List;

public class UserPurchasedItemDTO {

    UserOrder userOrder;
    List<ProductDTO> productDTOList;

    public UserOrder getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(UserOrder userOrder) {
        this.userOrder = userOrder;
    }

    public List<ProductDTO> getProductDTOList() {
        return productDTOList;
    }

    public void setProductDTOList(List<ProductDTO> productDTOList) {
        this.productDTOList = productDTOList;
    }
}
