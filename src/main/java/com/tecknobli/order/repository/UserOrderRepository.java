package com.tecknobli.order.repository;

import com.tecknobli.order.entity.UserOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrderRepository extends CrudRepository<UserOrder,String> {

    @Query(value = "FROM UserOrder WHERE userId = ?1 ")
    List<UserOrder> findByUserId(String userid);

    @Query("FROM UserOrder WHERE userOrderId = ?1 ")
    UserOrder findByOrderId(String orderId);
}
