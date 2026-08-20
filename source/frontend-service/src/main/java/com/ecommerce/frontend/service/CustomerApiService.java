package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.AddressDTO;
import com.ecommerce.frontend.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CustomerApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${CUSTOMER_SERVICE_URL:http://localhost:8082/api/members}")
    private String customerServiceUrl;

    @Value("${ADDRESS_SERVICE_URL:http://localhost:8082/api/addresses}")
    private String addressServiceUrl;

    public void createMember(String username, String password) {
        try {
            MemberDTO member = new MemberDTO();
            member.setUsername(username);
            member.setPassword(password);
            restTemplate.postForObject(customerServiceUrl, member, MemberDTO.class);
        } catch (RestClientException e) {
            System.err.println("Lỗi tạo Member: " + e.getMessage());
        }
    }

    public MemberDTO getMemberByUsername(String username) {
        try {
            return restTemplate.getForObject(customerServiceUrl + "/username/" + username, MemberDTO.class);
        } catch (RestClientException e) {
            System.err.println("Lỗi lấy Member: " + e.getMessage());
            return null;
        }
    }

    public List<AddressDTO> getAddressesByMember(String memberId) {
        try {
            AddressDTO[] addresses = restTemplate.getForObject(
                    addressServiceUrl + "/member/" + memberId, AddressDTO[].class);
            return addresses != null ? Arrays.asList(addresses) : List.of();
        } catch (RestClientException e) {
            System.err.println("Lỗi lấy danh sách địa chỉ: " + e.getMessage());
            return List.of();
        }
    }

    public void addAddress(AddressDTO address) {
        try {
            restTemplate.postForObject(addressServiceUrl, address, AddressDTO.class);
        } catch (RestClientException e) {
            System.err.println("Lỗi thêm địa chỉ: " + e.getMessage());
        }
    }

    public void deleteAddress(Integer addressId) {
        try {
            restTemplate.delete(addressServiceUrl + "/" + addressId);
        } catch (RestClientException e) {
            System.err.println("Lỗi xóa địa chỉ: " + e.getMessage());
        }
    }

    public List<MemberDTO> getAllMembers() {
        try {
            MemberDTO[] members = restTemplate.getForObject(customerServiceUrl, MemberDTO[].class);
            return members != null ? Arrays.asList(members) : List.of();
        } catch (RestClientException e) {
            System.err.println("Lỗi lấy danh sách thành viên: " + e.getMessage());
            return List.of();
        }
    }

    public boolean changeMemberPassword(String memberId, String oldPassword, String newPassword) {
        try {
            Map<String, String> body = Map.of("oldPassword", oldPassword, "newPassword", newPassword);
            restTemplate.put(customerServiceUrl + "/" + memberId + "/password", body);
            return true;
        } catch (RestClientException e) {
            System.err.println("Lỗi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }
}