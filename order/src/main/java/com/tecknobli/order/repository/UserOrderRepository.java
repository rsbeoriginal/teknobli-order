package com.tecknobli.order.repository;

import com.tecknobli.order.entity.UserOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrderRepository extends CrudRepository<UserOrder,String> {

    List<UserOrder> findByUserId(String userid);
}
