package com.tecknobli.order.repository;

import com.tecknobli.order.entity.Cart;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends CrudRepository<Cart,String> {
    List<Cart> findByUserId(String userId);

    @Modifying
    @Query("delete from cart where user_id = ?1")
    void deleteByUserId(String userId);
}
