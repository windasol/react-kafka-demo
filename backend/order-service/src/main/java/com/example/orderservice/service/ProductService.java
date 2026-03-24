package com.example.orderservice.service;

import com.example.orderservice.dto.ProductRequest;
import com.example.orderservice.entity.Product;
import com.example.orderservice.exception.ProductNotFoundException;
import com.example.orderservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 상품 비즈니스 로직 담당 서비스
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 상품 등록
     */
    public Product createProduct(ProductRequest request) {
        Objects.requireNonNull(request, "상품 요청 정보는 필수입니다.");
        Product product = Product.create(request.name(), request.price(), request.stock());
        return productRepository.save(product);
    }

    /**
     * 상품 목록 조회
     */
    public List<Product> getProducts() {
        return productRepository.findLatestProducts();
    }

    /**
     * 상품 단건 조회
     */
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * 상품 수정
     */
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.update(request.name(), request.price(), request.stock());
        return productRepository.save(product);
    }

    /**
     * 상품 삭제
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
