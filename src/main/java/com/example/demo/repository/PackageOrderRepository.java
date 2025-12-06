package com.example.demo.repository;


import com.example.demo.domain.PackageOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageOrderRepository extends JpaRepository<PackageOrder,Long> {
    Optional<PackageOrder> findByOrderCode(String orderCode);
    Optional<PackageOrder> findByRequestId(String requestId);
    List<PackageOrder> findByUserId(Long userId);
    List<PackageOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    /**
     * Tính tổng doanh thu từ các đơn hàng đã thanh toán
     */
    @Query("SELECT COALESCE(SUM(po.amount), 0.0) FROM PackageOrder po " +
            "WHERE po.paymentStatus = 'PAID' " +
            "AND po.createdAt BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenue(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Đếm tổng số gói đã bán (đã thanh toán)
     */
    @Query("SELECT COUNT(po) FROM PackageOrder po " +
            "WHERE po.paymentStatus = 'PAID' " +
            "AND po.createdAt BETWEEN :startDate AND :endDate")
    Long countTotalPackagesSold(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Thống kê theo từng loại gói (package type)
     * Return: [packageType, packageName, price, count, totalRevenue]
     */
    @Query("SELECT sp.packageType, sp.name, sp.price, COUNT(po), SUM(po.amount) " +
            "FROM PackageOrder po " +
            "JOIN po.servicePackage sp " +
            "WHERE po.paymentStatus = 'PAID' " +
            "AND po.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sp.packageType, sp.name, sp.price " +
            "ORDER BY SUM(po.amount) DESC")
    List<Object[]> findPackageTypeStatistics(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Thống kê doanh thu theo tuần
     * Return: [year, week, totalRevenue, packageCount]
     */
    @Query(value = "SELECT YEAR(created_at) as year, " +
            "WEEK(created_at, 1) as week, " +
            "SUM(amount) as revenue, " +
            "COUNT(*) as package_count " +
            "FROM package_orders " +
            "WHERE payment_status = 'PAID' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), WEEK(created_at, 1) " +
            "ORDER BY year, week",
            nativeQuery = true)
    List<Object[]> findRevenueByWeek(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Thống kê doanh thu theo tháng
     * Return: [year, month, totalRevenue, packageCount]
     */
    @Query(value = "SELECT YEAR(created_at) as year, " +
            "MONTH(created_at) as month, " +
            "SUM(amount) as revenue, " +
            "COUNT(*) as package_count " +
            "FROM package_orders " +
            "WHERE payment_status = 'PAID' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> findRevenueByMonth(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


}
