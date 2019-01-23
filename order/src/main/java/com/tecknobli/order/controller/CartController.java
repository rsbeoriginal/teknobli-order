package com.tecknobli.order.controller;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.tecknobli.order.dto.CartDTO;
import com.tecknobli.order.dto.UserCartDTO;
import com.tecknobli.order.entity.Cart;
import com.tecknobli.order.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    CartService cartService;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseEntity<CartDTO> add(@RequestBody CartDTO cartDTO){

        String uid = decodeToken(cartDTO.getIdToken());
        System.out.println("idToken: " + cartDTO.getIdToken());
        System.out.println("TID: " + uid);
        System.out.println("UID : " +cartDTO.getUserId());
        if(cartDTO.getUserId().equals(uid)) {
            System.out.println("Cart add");
            Cart cart = new Cart();
            BeanUtils.copyProperties(cartDTO, cart);
            Cart cartCreated = cartService.save(cart);
            CartDTO cartDTOCreated = new CartDTO();
            if (cartCreated !=null) {
                BeanUtils.copyProperties(cartCreated, cartDTOCreated);
                return new ResponseEntity<>(cartDTOCreated, HttpStatus.CREATED);
            }
        }
        return null;
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

    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    public ResponseEntity<Cart> updateCart(@RequestBody CartDTO cartDTO){
        Cart cart = new Cart();
        BeanUtils.copyProperties(cartDTO, cart);
        Cart cartCreated = cartService.update(cart);
        return new ResponseEntity<>(cartCreated,HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{cartId}",method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteEmplpyee(@PathVariable("cartId") String cartId){
        cartService.delete(cartId);
        return new ResponseEntity<>(Boolean.TRUE,HttpStatus.OK);
    }

    @GetMapping(value = "/user/{userId}")
    public UserCartDTO getCartByUser(@PathVariable("userId") String userId){
        return cartService.findByUserId(userId);
    }

    @DeleteMapping(value="/user/delete/{userID}")
    public ResponseEntity<Boolean> deleteUserCart(@PathVariable("userID") String userId){
        cartService.deleteByUserId(userId);
        return new ResponseEntity<>(Boolean.TRUE,HttpStatus.OK);
    }


}
