package com.tecknobli.order.dto;

import com.tecknobli.order.entity.UserOrder;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class PurchasedItemDTO {

    private String purchasedItemId;
    private UserOrder userOrderId;
    private String productId;
    private String merchantId;
    private int quantity;
    private float price;

    @Override
    public String toString() {
        return "PurchasedItemDTO{" +
                "purchasedItemId='" + purchasedItemId + '\'' +
                ", userOrderId=" + userOrderId +
                ", productId='" + productId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    public String getPurchasedItemId() {
        return purchasedItemId;
    }

    public void setPurchasedItemId(String purchasedItemId) {
        this.purchasedItemId = purchasedItemId;
    }

    public UserOrder getUserOrderId() {
        return userOrderId;
    }

    public void setUserOrderId(UserOrder userOrderId) {
        this.userOrderId = userOrderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
