package com.tecknobli.order.controller;


import com.tecknobli.order.dto.UserOrderDTO;
import com.tecknobli.order.dto.RecieptDTO;
import com.tecknobli.order.dto.UserPurchasedItemDTO;
import com.tecknobli.order.entity.UserOrder;
import com.tecknobli.order.service.UserOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@CrossOrigin
public class UserOrderController {

    @Autowired
    UserOrderService userOrderService;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseEntity<UserOrder> addOrder(@RequestBody UserOrderDTO userOrderDTO){
        UserOrder userOrder = new UserOrder();
        BeanUtils.copyProperties(userOrderDTO, userOrder);
        UserOrder orderCreated = userOrderService.save(userOrder);
        return new ResponseEntity<>(orderCreated,HttpStatus.CREATED);
    }

    @RequestMapping(value="/select/{userId}",method = RequestMethod.GET)
    public List<UserPurchasedItemDTO> getUserOrders(@PathVariable("userId") String userId){
        return userOrderService.findByUserId(userId);
    }

    @GetMapping(value = "/selectOrderById/{orderId}")
    public RecieptDTO selectOrderById(@PathVariable("orderId") String orderId){
        return userOrderService.findByOrderId(orderId);
    }


}
