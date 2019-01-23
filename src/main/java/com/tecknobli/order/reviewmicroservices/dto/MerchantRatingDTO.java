package com.tecknobli.order.reviewmicroservices.dto;

public class MerchantRatingDTO{
    private String merchantId;
    private String orderId;
    private String review;
    private String ratingId;
    private int rating;
    private String userId;

    public void setMerchantId(String merchantId){
        this.merchantId = merchantId;
    }

    public String getMerchantId(){
        return merchantId;
    }

    public void setOrderId(String orderId){
        this.orderId = orderId;
    }

    public String getOrderId(){
        return orderId;
    }

    public void setReview(String review){
        this.review = review;
    }

    public String getReview(){
        return review;
    }

    public void setRatingId(String ratingId){
        this.ratingId = ratingId;
    }

    public String getRatingId(){
        return ratingId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUserId(){
        return userId;
    }

    @Override
    public String toString(){
        return
                "MerchantRatingDTO{" +
                        "merchantId = '" + merchantId + '\'' +
                        ",orderId = '" + orderId + '\'' +
                        ",review = '" + review + '\'' +
                        ",ratingId = '" + ratingId + '\'' +
                        ",rating = '" + rating + '\'' +
                        ",userId = '" + userId + '\'' +
                        "}";
    }
}