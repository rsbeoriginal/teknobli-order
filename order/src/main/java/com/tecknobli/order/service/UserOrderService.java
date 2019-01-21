package com.tecknobli.order.service;

import com.tecknobli.order.dto.RecieptDTO;
import com.tecknobli.order.dto.UserPurchasedItemDTO;
import com.tecknobli.order.entity.UserOrder;

import java.util.List;

public interface UserOrderService {

    public UserOrder save(UserOrder userOrder);
    public UserOrder findOne(String orderID);
    public List<UserPurchasedItemDTO> findByUserId(String userId);

    RecieptDTO findByOrderId(String orderId);
}
