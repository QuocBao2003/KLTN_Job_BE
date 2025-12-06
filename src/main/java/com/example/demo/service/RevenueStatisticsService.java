package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.request.StatisticsFilterDTO;
import com.example.demo.dto.request.servicePackage.PackageRevenueStatisticsDTO;
import com.example.demo.repository.PackageOrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.PackageType;
import com.example.demo.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RevenueStatisticsService {

    private final PackageOrderRepository packageOrderRepository;
    private final UserRepository userRepository;

    public RevenueStatisticsService(PackageOrderRepository packageOrderRepository, UserRepository userRepository) {
        this.packageOrderRepository = packageOrderRepository;
        this.userRepository = userRepository;
    }

    public PackageRevenueStatisticsDTO getRevenueStatistics(StatisticsFilterDTO filter) {
        filter.validate();

        // Kiểm tra quyền Admin
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User adminUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!adminUser.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new RuntimeException("Only SUPER_ADMIN can access revenue statistics");
        }

        PackageRevenueStatisticsDTO statistics = new PackageRevenueStatisticsDTO();

        // 1. Tính tổng doanh thu
        Double totalRevenue = packageOrderRepository.calculateTotalRevenue(
                filter.getStartDate(),
                filter.getEndDate()
        );
        statistics.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // 2. Đếm tổng số gói đã bán
        Long totalPackagesSold = packageOrderRepository.countTotalPackagesSold(
                filter.getStartDate(),
                filter.getEndDate()
        );
        statistics.setTotalPackagesSold(totalPackagesSold != null ? totalPackagesSold : 0L);

        // 3. Thống kê chi tiết theo từng loại gói
        List<Object[]> packageTypeData = packageOrderRepository.findPackageTypeStatistics(
                filter.getStartDate(),
                filter.getEndDate()
        );

        List<PackageRevenueStatisticsDTO.PackageTypeStatistic> packageTypeStats =
                packageTypeData.stream()
                        .map(row -> {
                            PackageType packageType = (PackageType) row[0];
                            String packageName = (String) row[1];
                            Double price = (Double) row[2];
                            Long quantitySold = ((Number) row[3]).longValue();
                            Double packageRevenue = row[4] != null ?
                                    ((Number) row[4]).doubleValue() : 0.0;

                            // Tính % so với tổng doanh thu
                            Double percentage = totalRevenue > 0 ?
                                    (packageRevenue / totalRevenue) * 100 : 0.0;

                            return new PackageRevenueStatisticsDTO.PackageTypeStatistic(
                                    packageType,
                                    packageName,
                                    price,
                                    quantitySold,
                                    packageRevenue,
                                    Math.round(percentage * 100.0) / 100.0 // Làm tròn 2 chữ số
                            );
                        })
                        .collect(Collectors.toList());

        statistics.setPackageTypeStatistics(packageTypeStats);

        // 4. Thống kê theo thời gian
        if ("WEEK".equalsIgnoreCase(filter.getTimeUnit())) {
            List<Object[]> weeklyData = packageOrderRepository.findRevenueByWeek(
                    filter.getStartDate(),
                    filter.getEndDate()
            );
            statistics.setRevenueTimeSeries(buildWeeklyRevenueSeries(weeklyData));
        } else {
            List<Object[]> monthlyData = packageOrderRepository.findRevenueByMonth(
                    filter.getStartDate(),
                    filter.getEndDate()
            );
            statistics.setRevenueTimeSeries(buildMonthlyRevenueSeries(monthlyData));
        }

        // 5. Set metadata
        statistics.setStatisticsTime(Instant.now());
        statistics.setFilterStartDate(filter.getStartDate());
        statistics.setFilterEndDate(filter.getEndDate());
        statistics.setTimeUnit(filter.getTimeUnit());

        return statistics;
    }

    /**
     * Build time series theo tuần
     */
    private List<PackageRevenueStatisticsDTO.RevenueTimeSeriesStatistic>
    buildWeeklyRevenueSeries(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int week = ((Number) row[1]).intValue();
                    Double revenue = row[2] != null ?
                            ((Number) row[2]).doubleValue() : 0.0;
                    Long packageCount = ((Number) row[3]).longValue();

                    String label = String.format("Week %d-%d", week, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, 1, 1, 0, 0)
                            .plusWeeks(week - 1);
                    LocalDateTime periodEnd = periodStart.plusWeeks(1).minusSeconds(1);

                    return new PackageRevenueStatisticsDTO.RevenueTimeSeriesStatistic(
                            label,
                            revenue,
                            packageCount,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Build time series theo tháng
     */
    private List<PackageRevenueStatisticsDTO.RevenueTimeSeriesStatistic>
    buildMonthlyRevenueSeries(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int month = ((Number) row[1]).intValue();
                    Double revenue = row[2] != null ?
                            ((Number) row[2]).doubleValue() : 0.0;
                    Long packageCount = ((Number) row[3]).longValue();

                    String label = String.format("%02d/%d", month, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, month, 1, 0, 0);
                    LocalDateTime periodEnd = periodStart.plusMonths(1).minusSeconds(1);

                    return new PackageRevenueStatisticsDTO.RevenueTimeSeriesStatistic(
                            label,
                            revenue,
                            packageCount,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }
}
