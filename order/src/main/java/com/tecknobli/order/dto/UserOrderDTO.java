package com.tecknobli.order.dto;

import java.util.Date;

public class UserOrderDTO {

    private String  userOrderId;
    private String userId;
    private String address;
    private String phoneNo;
    private String emailId;
    private Date orderTimeStamp;


    @Override
    public String toString() {
        return "UserOrderDTO{" +
                "userOrderId='" + userOrderId + '\'' +
                ", UserId='" + userId + '\'' +
                ", address='" + address + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", orderTimeStamp=" + orderTimeStamp +
                '}';
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUserOrderId() {
        return userOrderId;
    }

    public void setUserOrderId(String userOrderId) {
        this.userOrderId = userOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Date getOrderTimeStamp() {
        return orderTimeStamp;
    }

    public void setOrderTimeStamp(Date orderTimeStamp) {
        this.orderTimeStamp = orderTimeStamp;
    }
}
