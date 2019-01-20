package com.tecknobli.order.repository;

import com.tecknobli.order.entity.UserOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrderRepository implements CrudRepository<UserOrder,String> {
}
