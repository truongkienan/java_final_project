package com.ecommerce.frontend.service;

import com.ecommerce.frontend.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ecommerce.frontend.dto.CategoryDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class CatalogApiService {

    @Autowired
    private RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${CATALOG_SERVICE_URL:http://localhost:8081/api/products}")
    private String catalogServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${CATEGORY_SERVICE_URL:http://localhost:8081/api/categories}")
    private String categoryServiceUrl;

    public List<ProductDTO> getProducts() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

            ResponseEntity<ProductDTO[]> responseEntity = restTemplate.exchange(
                    catalogServiceUrl,
                    HttpMethod.GET,
                    requestEntity,
                    ProductDTO[].class);

            System.out.println("Mã trạng thái trả về: " + responseEntity.getStatusCode());
            ProductDTO[] products = responseEntity.getBody();
            return products != null ? Arrays.asList(products) : Arrays.asList();
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Lỗi kết nối Catalog Service: " + e.getMessage());
            return Arrays.asList();
        }
    }

    public List<com.ecommerce.frontend.dto.CategoryDTO> getAllCategories() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<com.ecommerce.frontend.dto.CategoryDTO[]> response = restTemplate.exchange(
                categoryServiceUrl,
                HttpMethod.GET,
                requestEntity,
                com.ecommerce.frontend.dto.CategoryDTO[].class);
        com.ecommerce.frontend.dto.CategoryDTO[] categories = response.getBody();
        return categories != null ? Arrays.asList(categories) : Arrays.asList();
    }

    public List<CategoryDTO> getCategoryTree() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<CategoryDTO[]> response = restTemplate.exchange(
                    categoryServiceUrl + "/tree",
                    HttpMethod.GET,
                    requestEntity,
                    CategoryDTO[].class);
            CategoryDTO[] tree = response.getBody();
            return tree != null ? Arrays.asList(tree) : Arrays.asList();
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Lỗi kết nối Catalog Service (category tree): " + e.getMessage());
            return Arrays.asList();
        }
    }

    public CategoryDTO getCategoryById(Short id) {
        return restTemplate.getForObject(categoryServiceUrl + "/" + id, CategoryDTO.class);
    }

    public void saveCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getCategoryId() == null) {
            restTemplate.postForObject(categoryServiceUrl, categoryDTO, CategoryDTO.class);
        } else {
            restTemplate.put(categoryServiceUrl + "/" + categoryDTO.getCategoryId(), categoryDTO);
        }
    }

    public ProductDTO getProductById(Integer id) {
        return restTemplate.getForObject(catalogServiceUrl + "/" + id, ProductDTO.class);
    }

    public void saveProduct(ProductDTO productDTO) {
        if (productDTO.getProductId() == null) {
            restTemplate.postForObject(catalogServiceUrl, productDTO, ProductDTO.class);
        } else {
            restTemplate.put(catalogServiceUrl + "/" + productDTO.getProductId(), productDTO);
        }
    }

    public void deleteCategory(Short id) {
        restTemplate.delete(categoryServiceUrl + "/" + id);
    }

    public void deleteProduct(Integer id) {
        restTemplate.delete(catalogServiceUrl + "/" + id);
    }

}