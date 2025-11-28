package com.example.demo.repository;


import com.example.demo.domain.PackageOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageOrderRepository extends JpaRepository<PackageOrder,Long> {
    Optional<PackageOrder> findByOrderCode(String orderCode);
    Optional<PackageOrder> findByRequestId(String requestId);
    List<PackageOrder> findByUserId(Long userId);
    List<PackageOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

}
