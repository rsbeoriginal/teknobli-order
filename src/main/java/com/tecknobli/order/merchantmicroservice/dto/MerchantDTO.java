package com.tecknobli.order.merchantmicroservice.dto;

public class MerchantDTO {
    private String merchantId;
    private String merchantName;
    private Double rating = 5d;

    public void setMerchantId(String merchantId){
        this.merchantId = merchantId;
    }

    public String getMerchantId(){
        return merchantId;
    }

    public void setMerchantName(String merchantName){
        this.merchantName = merchantName;
    }

    public String getMerchantName(){
        return merchantName;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
