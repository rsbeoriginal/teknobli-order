package com.tecknobli.order.controller;


import com.tecknobli.order.dto.CartDTO;
import com.tecknobli.order.dto.UserCartDTO;
import com.tecknobli.order.entity.Cart;
import com.tecknobli.order.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseEntity<Cart> add(@RequestBody CartDTO cartDTO){
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartDTO, cart);
        Cart cartCreated = cartService.save(cart);
        return new ResponseEntity<>(cartCreated,HttpStatus.CREATED);

    }

    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    public ResponseEntity<Cart> updateEmployee(@RequestBody CartDTO cartDTO){
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartDTO, cart);
        Cart cartCreated = cartService.save(cart);
        return new ResponseEntity<>(cartCreated,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{cartId}",method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteEmplpyee(@PathVariable("cartId") String cartId){
        cartService.delete(cartId);
        return new ResponseEntity<>(Boolean.TRUE,HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}")
    public List<UserCartDTO> getCartByUser(@PathVariable("userId") String userId){
        return cartService.findByUserId(userId);
    }

    @RequestMapping(value="/user/delete/{userID}")
    public ResponseEntity<Boolean> deleteUserCart(@PathVariable("userID") String userId){
        cartService.deleteByUserId(userId);
        return new ResponseEntity<>(Boolean.TRUE,HttpStatus.OK);
    }

}
