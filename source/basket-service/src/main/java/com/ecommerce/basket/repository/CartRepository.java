package com.ecommerce.basket.repository;

import com.ecommerce.basket.entity.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {

}
