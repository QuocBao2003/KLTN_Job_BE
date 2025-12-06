package com.example.demo.controller;

import com.example.demo.dto.request.StatisticsFilterDTO;
import com.example.demo.dto.request.servicePackage.PackageRevenueStatisticsDTO;
import com.example.demo.service.RevenueStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/statistics")
public class RevenueStatisticsController {

    private final RevenueStatisticsService revenueStatisticsService;

    public RevenueStatisticsController(RevenueStatisticsService revenueStatisticsService) {
        this.revenueStatisticsService = revenueStatisticsService;
    }
    /**
     * API cho Admin xem thống kê doanh thu từ bán gói dịch vụ
     * GET /api/v1/statistics/revenue
     *
     * Query params:
     * - startDate: Ngày bắt đầu (ISO format) - Optional
     * - endDate: Ngày kết thúc (ISO format) - Optional
     * - timeUnit: "WEEK" hoặc "MONTH" (mặc định "MONTH")
     *
     * Examples:
     * /api/v1/statistics/revenue
     * /api/v1/statistics/revenue?timeUnit=MONTH
     * /api/v1/statistics/revenue?timeUnit=WEEK&startDate=2024-01-01T00:00:00Z&endDate=2025-01-01T00:00:00Z
     *
     * Response:
     * {
     *   "totalRevenue": 15500000.0,
     *   "totalPackagesSold": 25,
     *   "packageTypeStatistics": [
     *     {
     *       "packageType": "FEATURED_JOB",
     *       "packageName": "Công việc hấp dẫn - Ưu tiên hiển thị - Tiêu đề nổi bật",
     *       "price": 800000.0,
     *       "quantitySold": 10,
     *       "totalRevenue": 8000000.0,
     *       "percentageOfTotal": 51.61
     *     },
     *     ...
     *   ],
     *   "revenueTimeSeries": [...],
     *   "statisticsTime": "2025-01-15T10:30:00Z",
     *   "filterStartDate": "2024-01-15T00:00:00Z",
     *   "filterEndDate": "2025-01-15T23:59:59Z",
     *   "timeUnit": "MONTH"
     * }
     */
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<PackageRevenueStatisticsDTO> getRevenueStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "MONTH") String timeUnit
    ) {
        StatisticsFilterDTO filter = new StatisticsFilterDTO();

        if (startDate != null && !startDate.isEmpty()) {
            filter.setStartDate(Instant.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            filter.setEndDate(Instant.parse(endDate));
        }
        filter.setTimeUnit(timeUnit);

        PackageRevenueStatisticsDTO statistics =
                revenueStatisticsService.getRevenueStatistics(filter);

        return ResponseEntity.ok(statistics);
    }
}
