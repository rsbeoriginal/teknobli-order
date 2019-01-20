package com.tecknobli.order.dto;

public class CartDTO {

    private String cartId;
    private String UserId;
    private String ProductId;
    private String merchantId;
    private int quantity;

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
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

    @Override
    public String toString() {
        return "CartDTO{" +
                "cartId='" + cartId + '\'' +
                ", UserId='" + UserId + '\'' +
                ", ProductId='" + ProductId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
