package com.tecknobli.order.controller;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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
        System.out.println("OrderAdd:");
        String uid = decodeToken(userOrderDTO.getIdToken());
        System.out.println("idToken: " + userOrderDTO.getIdToken());
        System.out.println("TID: " + uid);
        System.out.println("UID : " +userOrderDTO.getUserId());
        if(decodeToken(userOrderDTO.getIdToken()).equals(userOrderDTO.getUserId())) {
            UserOrder userOrder = new UserOrder();
            BeanUtils.copyProperties(userOrderDTO, userOrder);
            UserOrder orderCreated = userOrderService.save(userOrder);
            return new ResponseEntity<>(orderCreated, HttpStatus.CREATED);
        }
        return null;
    }

    @RequestMapping(value="/select/{userId}",method = RequestMethod.GET)
    public List<UserPurchasedItemDTO> getUserOrders(@PathVariable("userId") String userId){
        return userOrderService.findByUserId(userId);
    }

    @GetMapping(value = "/selectOrderById/{orderId}")
    public RecieptDTO selectOrderById(@PathVariable("orderId") String orderId){
        return userOrderService.findByOrderId(orderId);
    }

    private String decodeToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
        }
        return null;
    }


}
