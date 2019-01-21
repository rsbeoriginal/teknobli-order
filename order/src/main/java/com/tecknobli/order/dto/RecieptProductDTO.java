package com.tecknobli.order.dto;

import com.tecknobli.order.merchantmicroservice.dto.MerchantDTO;

public class RecieptProductDTO {

    private String productId;
    private String productName;
    private String imageUrl;
    private Double price;
    private int quantity;
    private String merchantId;
    private String merchantName;

    public void setProductData(ProductDTO productDTO) {
        this.productId = productDTO.getProductId();
        this.productName = productDTO.getProductName();
        this.imageUrl = productDTO.getImageUrl();
    }



    public void setMerchantData(MerchantDTO merchantDTO) {
        this.merchantId = merchantDTO.getMerchantId();
        this.merchantName = merchantDTO.getMerchantName();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}
