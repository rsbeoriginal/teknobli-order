package com.tecknobli.order.service.CartServiceImpl;

import com.tecknobli.order.dto.UserCartDTO;
import com.tecknobli.order.entity.Cart;
import com.tecknobli.order.repository.CartRepository;
import com.tecknobli.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Override
    @Transactional(readOnly = false)
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String cartId) {
        cartRepository.delete(cartId);
    }

    @Override
    @Transactional(readOnly = false)
    public Cart update(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public List<UserCartDTO> findByUserId(String userId) {

        List<Cart> userCart = cartRepository.findByUserId(userId);
        List<UserCartDTO> userCartDTOList = new ArrayList<>();
        for(Cart cart : userCart){

        }
        return userCartDTOList;
    }

    @Override
    public void deleteByUserId(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
