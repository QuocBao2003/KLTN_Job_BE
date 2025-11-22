package com.example.demo.controller;

import com.example.demo.dto.request.AdminStatisticsDTO;
import com.example.demo.dto.request.HRStatisticsDTO;
import com.example.demo.dto.request.StatisticsFilterDTO;
import com.example.demo.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * API cho HR xem thống kê của company
     * GET /api/v1/statistics/hr
     *
     * Query params:
     * - startDate: Ngày bắt đầu (ISO format)
     * - endDate: Ngày kết thúc (ISO format)
     * - timeUnit: "WEEK" hoặc "MONTH" (mặc định "MONTH")
     *
     * Examples:
     * /api/v1/statistics/hr?timeUnit=MONTH
     * /api/v1/statistics/hr?timeUnit=WEEK&startDate=2024-01-01T00:00:00Z&endDate=2025-01-01T00:00:00Z
     */
    @PreAuthorize("hasAuthority('HR')")
    @GetMapping("/hr")
    public ResponseEntity<HRStatisticsDTO> getHRStatistics(
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

        HRStatisticsDTO statistics = statisticsService.getHRStatistics(filter);
        return ResponseEntity.ok(statistics);
    }

    /**
     * API cho Admin xem thống kê toàn hệ thống
     * GET /api/v1/statistics/admin
     *
     * Query params:
     * - startDate: Ngày bắt đầu (ISO format)
     * - endDate: Ngày kết thúc (ISO format)
     * - timeUnit: "WEEK" hoặc "MONTH" (mặc định "MONTH")
     * - topLimit: Số lượng top companies (mặc định 10, max 100)
     *
     * Examples:
     * /api/v1/statistics/admin?timeUnit=MONTH&topLimit=10
     * /api/v1/statistics/admin?timeUnit=WEEK&topLimit=20
     */
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<AdminStatisticsDTO> getAdminStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "MONTH") String timeUnit,
            @RequestParam(required = false, defaultValue = "10") Integer topLimit
    ) {
        StatisticsFilterDTO filter = new StatisticsFilterDTO();

        if (startDate != null && !startDate.isEmpty()) {
            filter.setStartDate(Instant.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            filter.setEndDate(Instant.parse(endDate));
        }
        filter.setTimeUnit(timeUnit);
        filter.setTopLimit(topLimit);

        AdminStatisticsDTO statistics = statisticsService.getAdminStatistics(filter);
        return ResponseEntity.ok(statistics);
    }

}
