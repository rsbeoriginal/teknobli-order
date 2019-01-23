package com.tecknobli.order.repository;

import com.tecknobli.order.entity.Cart;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {
    List<Cart> findByUserId(String userId);

    @Modifying
    @Query(value = "delete from Cart WHERE userId = ?1")
    void deleteByUserId(String userId);

    @Query(value = "FROM Cart WHERE (userId = ?1 AND productId = ?2 AND merchantId =?3)")
    Cart findByUserIdAndProductIdAndMerchantId(String userId, String productId, String merchantId);

    @Modifying
    @Query(value = "UPDATE Cart SET quantity = ?2 WHERE cartId = ?1")
    void updateCartQuantity(String cartId, int quantity);
}
