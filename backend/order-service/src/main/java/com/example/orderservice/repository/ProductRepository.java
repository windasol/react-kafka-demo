package com.example.orderservice.repository;

import com.example.orderservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 상품 저장소 - 데이터 접근만 담당한다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 최신 상품 순으로 전체 목록 조회
     */
    List<Product> findAllByOrderByCreatedAtDesc();
}
