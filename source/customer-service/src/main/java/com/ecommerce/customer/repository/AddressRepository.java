package com.ecommerce.customer.repository;

import com.ecommerce.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    // Lấy tất cả địa chỉ của một Member
    List<Address> findByMemberId(UUID memberId);
}