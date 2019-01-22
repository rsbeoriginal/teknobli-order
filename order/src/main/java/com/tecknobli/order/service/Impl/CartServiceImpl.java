package com.tecknobli.order.service.Impl;

import com.tecknobli.order.dto.ProductDTO;
import com.tecknobli.order.dto.UserCartDTO;
import com.tecknobli.order.entity.Cart;
import com.tecknobli.order.merchantmicroservice.dto.MerchantProductDTO;
import com.tecknobli.order.productmicroservice.Endpoints;
import com.tecknobli.order.repository.CartRepository;
import com.tecknobli.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
        Cart cartValidate = cartRepository.findByUserIdAndProductIdAndMerchantId(cart.getUserId(), cart.getProductId(), cart.getMerchantId());
        if (cartValidate == null) {
            return cartRepository.save(cart);
        }
        return null;
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
    public UserCartDTO findByUserId(String userId) {

        List<Cart> userCart = cartRepository.findByUserId(userId);
        UserCartDTO userCartDTO = new UserCartDTO();
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Cart cart : userCart) {
            ProductDTO productDTO = getProduct(cart.getProductId());
            productDTO.setMerchantId(cart.getMerchantId());
            productDTO.setQuantity(cart.getQuantity());
            productDTO.setCartId(cart.getCartId());
            productDTO.setPrice(getPriceFromMercant(cart.getMerchantId(), cart.getProductId()));
            productDTOList.add(productDTO);
        }
        userCartDTO.setUserId(userId);
        userCartDTO.setProducts(productDTOList);
        return userCartDTO;
    }

    private float getPriceFromMercant(String merchantId, String productId) {
        RestTemplate restTemplate = new RestTemplate();
        MerchantProductDTO result = restTemplate.getForObject(Endpoints.MERCHANT_BASE_URL + Endpoints.MERCHANT_PRODUCT + merchantId + "/" + productId, MerchantProductDTO.class);
        return result.getPrice();
    }

    public ProductDTO getProduct(String productId) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(productId);
        ProductDTO result = restTemplate.getForObject(Endpoints.BASE_URL + Endpoints.SINGLRPRODUCT_URL + productId, ProductDTO.class);
        if (result != null) {
            return result;
        }
        return null;
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteByUserId(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
