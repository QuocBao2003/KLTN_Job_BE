package com.example.demo.dto.request.servicePackage;

import com.example.demo.util.Enum.PackageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageRevenueStatisticsDTO {

    private Double totalRevenue;
    private Long totalPackagesSold;

    private List<PackageTypeStatistic> packageTypeStatistics;
    private List<RevenueTimeSeriesStatistic> revenueTimeSeries;

    private Instant statisticsTime;
    private Instant filterStartDate;
    private Instant filterEndDate;
    private String timeUnit;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PackageTypeStatistic {
        private PackageType packageType;
        private String packageName;
        private Double price;
        private Long quantitySold;
        private Double totalRevenue;
        private Double percentageOfTotal; // % so với tổng doanh thu
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueTimeSeriesStatistic {
        private String label;
        private Double revenue;
        private Long packagesSold;
        private Instant periodStart;
        private Instant periodEnd;
    }
}
