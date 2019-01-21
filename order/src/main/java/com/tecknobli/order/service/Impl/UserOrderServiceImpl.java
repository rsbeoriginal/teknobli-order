package com.tecknobli.order.service.Impl;

import com.tecknobli.order.dto.*;
import com.tecknobli.order.entity.PurchasedItem;
import com.tecknobli.order.entity.UserOrder;
import com.tecknobli.order.merchantmicroservice.dto.MerchantDTO;
import com.tecknobli.order.merchantmicroservice.dto.MerchantOrderDTO;
import com.tecknobli.order.productmicroservice.Endpoints;
import com.tecknobli.order.repository.PurchasedItemRepository;
import com.tecknobli.order.repository.UserOrderRepository;
import com.tecknobli.order.service.CartService;
import com.tecknobli.order.service.EmailService;
import com.tecknobli.order.service.UserOrderService;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    EmailService emailService;

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
        //Send Mail to user
        sendMailToUser(userOrderCreated);
        sendOrderToMerchant(userOrderCreated,productDTOList);
        return userOrderCreated;
    }

    private void sendOrderToMerchant(UserOrder userOrderCreated, List<ProductDTO> productDTOList) {

//        List<MerchantOrderDTO> merchantOrderDTOList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        for (ProductDTO productDTO: productDTOList){
            MerchantOrderDTO merchantOrderDTO = new MerchantOrderDTO();
            merchantOrderDTO.setMerchantId(productDTO.getMerchantId());
            merchantOrderDTO.setProductId(productDTO.getProductId());
            merchantOrderDTO.setOrderId(userOrderCreated.getUserOrderId());

            String URL = com.tecknobli.order.merchantmicroservice.Endpoints.BASE_URL + com.tecknobli.order.merchantmicroservice.Endpoints.ADDORDER_URL;
            restTemplate.postForEntity(URL,merchantOrderDTO,MerchantOrderDTO.class);
        }


    }

    private void sendMailToUser(UserOrder userOrderCreated) {

        String subject = "Your Order: " +userOrderCreated.getUserOrderId();
        String body="";
        Double totalPrice =0d;

        for (PurchasedItem purchanteItem: userOrderCreated.getPurchasedItemList()) {

            ProductDTO productDTO = getProduct(purchanteItem.getProductId());
            body += "\nProduct Name: " + productDTO.getProductName();
            body += "\nPrice: " +purchanteItem.getPrice();
            body += "\nQuantity: " + purchanteItem.getQuantity();

            totalPrice += (purchanteItem.getPrice()*purchanteItem.getQuantity());
        }

        body += "\n\nTotal : " + totalPrice;


        emailService.sendSimpleMessage(userOrderCreated.getEmailId(),
                subject,
                body);


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

    @Override
    public RecieptDTO findByOrderId(String orderId) {

        RecieptDTO recieptDTO = new RecieptDTO();
        UserOrder userOrder = userOrderRepository.findOne(orderId);
        recieptDTO.setUserOrderData(userOrder);
        List<RecieptProductDTO> recieptProductDTOList= new ArrayList<>();
        for(PurchasedItem purchasedItem : userOrder.getPurchasedItemList()){
            RecieptProductDTO recieptProductDTO = new RecieptProductDTO();
            ProductDTO productDTO = getProduct(purchasedItem.getProductId());
            MerchantDTO merchantDTO =getMerchant(purchasedItem.getMerchantId());
            recieptProductDTO.setProductData(productDTO);
            recieptProductDTO.setMerchantData(merchantDTO);
            recieptProductDTO.setPrice(Double.valueOf(purchasedItem.getPrice()));
            recieptProductDTO.setQuantity(purchasedItem.getQuantity());
        }
        return recieptDTO;
    }

    private MerchantDTO getMerchant(String merchantId) {

        RestTemplate restTemplate = new RestTemplate();
        System.out.println(merchantId);
        MerchantDTO result = restTemplate.getForObject(com.tecknobli.order.merchantmicroservice.Endpoints.BASE_URL + com.tecknobli.order.merchantmicroservice.Endpoints.SINGLE_MERCHANT_URL + merchantId, MerchantDTO.class);
        return result;
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
