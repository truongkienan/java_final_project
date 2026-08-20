package com.ecommerce.customer.controller;

import com.ecommerce.customer.entity.Address;
import com.ecommerce.customer.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/member/{memberId}")
    public List<Address> getByMember(@PathVariable("memberId") UUID memberId) {
        return addressRepository.findByMemberId(memberId);
    }

    @PostMapping
    public Address createAddress(@RequestBody Address address) {
        return addressRepository.save(address);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable("id") Integer id) {
        addressRepository.deleteById(id);
    }
}