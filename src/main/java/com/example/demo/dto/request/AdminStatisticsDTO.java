package com.example.demo.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatisticsDTO {
    private long totalApprovedJobs;
    private long totalRejectedJobs;
    private long totalPendingJobs;
    private long totalCompanies;

    // Company có nhiều resume nhất
    private CompanyTopResume topCompanyByResumes;

    // Thống kê resume theo status
    private List<ResumeStatusStatistic> resumesByStatus;

    // Thống kê theo thời gian (tuần hoặc tháng)
    private List<TimeSeriesStatistic> jobsTimeSeries;
    private List<TimeSeriesStatistic> resumesTimeSeries;

    // Thống kê resume theo company (top N)
    private List<CompanyResumeStatistic> companyResumeStatistics;

    // Metadata
    private Instant statisticsTime;
    private Instant filterStartDate;
    private Instant filterEndDate;
    private String timeUnit; // "WEEK" hoặc "MONTH"

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyTopResume {
        private Long companyId;
        private String companyName;
        private String companyLogo;
        private long totalResumes;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyResumeStatistic {
        private Long companyId;
        private String companyName;
        private long totalResumes;
        private long approvedResumes;
        private long rejectedResumes;
        private long pendingResumes;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumeStatusStatistic {
        private String status;
        private long count;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSeriesStatistic {
        private String label; // "Week 1-2025", "Week 2-2025" hoặc "01/2025"
        private long count;
        private Instant periodStart;
        private Instant periodEnd;
    }

}
