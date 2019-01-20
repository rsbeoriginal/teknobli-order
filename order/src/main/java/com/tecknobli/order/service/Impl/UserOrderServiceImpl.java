package com.tecknobli.order.service.Impl;

import com.sun.javafx.UnmodifiableArrayList;
import com.tecknobli.order.dto.ProductDTO;
import com.tecknobli.order.dto.UserPurchasedItemDTO;
import com.tecknobli.order.entity.PurchasedItem;
import com.tecknobli.order.entity.UserOrder;
import com.tecknobli.order.productmicroservice.Endpoints;
import com.tecknobli.order.repository.PurchasedItemRepository;
import com.tecknobli.order.repository.UserOrderRepository;
import com.tecknobli.order.service.CartService;
import com.tecknobli.order.service.UserOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    UserOrderRepository userOrderRepository;

    @Autowired
    PurchasedItemRepository purchasedItemRepository;

    @Autowired
    CartService cartService;

    @Override
    @Transactional(readOnly = false)
    public UserOrder save(UserOrder userOrder) {

        UserOrder userOrderCreated = userOrderRepository.save(userOrder);
        String userId = userOrder.getUserId();
        List<ProductDTO> productDTOList = cartService.findByUserId(userId).getProducts();
        for(ProductDTO productDTO : productDTOList){
            PurchasedItem purchasedItem = new PurchasedItem();
            purchasedItem.setMerchantId(productDTO.getMerchantId());
            purchasedItem.setPrice(productDTO.getPrice());
            purchasedItem.setProductId(productDTO.getProductId());
            purchasedItem.setQuantity(productDTO.getQuantity());
            purchasedItem.setUserOrderId(userOrder);
            purchasedItemRepository.save(purchasedItem);
        }
        cartService.deleteByUserId(userId);
        return userOrderCreated;
    }

    @Override
    public UserOrder findOne(String orderID) {
        return userOrderRepository.findOne(orderID);
    }

    @Override
    public List<UserPurchasedItemDTO> findByUserId(String userId) {

        List<UserPurchasedItemDTO> userPurchasedItemDTOS = new ArrayList<>();

        for(UserOrder userOrder : userOrderRepository.findByUserId(userId)){

            System.out.println(userOrder.getUserOrderId());
            UserPurchasedItemDTO temp = new UserPurchasedItemDTO();
            temp.setUserOrder(userOrder);
            List<PurchasedItem> purchasedItemList = purchasedItemRepository.findByUserOrderId(userOrder.getUserOrderId());
            List<ProductDTO> productDTOList = new ArrayList<>();
            for(PurchasedItem purchasedItem : purchasedItemList){
                ProductDTO productDTO = getProduct(purchasedItem.getProductId());
                productDTO.setQuantity(purchasedItem.getQuantity());

                productDTOList.add(productDTO);
            }
            temp.setProductDTOList(productDTOList);
            userPurchasedItemDTOS.add(temp);
        }
        return userPurchasedItemDTOS;
    }

    public ProductDTO getProduct(String productId){
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(productId);
        ProductDTO result = restTemplate.getForObject(Endpoints.BASE_URL + Endpoints.SINGLRPRODUCT_URL + productId, ProductDTO.class);
        if(result != null){
            return result;
        }
        return  null;
    }



}
