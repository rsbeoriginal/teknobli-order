package com.tecknobli.order.service;

import com.tecknobli.order.entity.Cart;

import java.util.List;

public interface CartService {

    public Cart save(Cart employee);
    public void delete(String cartId);
    public Cart update(Cart cart);
    public List<Cart> findByUserId(String userId);
    public void deleteByUserId(String userId);

}
