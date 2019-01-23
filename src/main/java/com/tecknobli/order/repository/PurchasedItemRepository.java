package com.tecknobli.order.repository;

import com.tecknobli.order.dto.ProductDTO;
import com.tecknobli.order.entity.PurchasedItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchasedItemRepository extends CrudRepository<PurchasedItem,String> {

    @Query(value = "FROM PurchasedItem WHERE order_id = ?1")
    List<PurchasedItem> findByUserOrderId(String orderId);

}
