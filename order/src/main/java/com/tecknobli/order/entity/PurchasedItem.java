package com.tecknobli.order.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name=PurchasedItem.TABLE_NAME)
public class PurchasedItem {

    public static final String TABLE_NAME = "PURCHASEDITEM";
    private static final String ID_COLUMN = "PURCHASED_ITEM_ID";

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name="uuid",strategy = "uuid2")
    @Column(name = PurchasedItem.ID_COLUMN)
    private String purchasedItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="order_id")
    private UserOrder userOrderId;
    private String productId;
    private String merchantId;
    private int quantity;
    private float price;

    @Override
    public String toString() {
        return "PurchasedItem{" +
                "purchasedItemId='" + purchasedItemId + '\'' +
                ", userOrderId=" + userOrderId +
                ", productId='" + productId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getIdColumn() {
        return ID_COLUMN;
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
