package com.tecknobli.order.merchantmicroservice.dto;

public class MerchantProductDTO {
    private String merchantProductId;
    private String productId;
    private MerchantDTO merchant;
    private float price;
    private int stock;

    public void setMerchantProductId(String merchantProductId){
        this.merchantProductId = merchantProductId;
    }

    public String getMerchantProductId(){
        return merchantProductId;
    }

    public void setProductId(String productId){
        this.productId = productId;
    }

    public String getProductId(){
        return productId;
    }

    public MerchantDTO getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantDTO merchant) {
        this.merchant = merchant;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

}