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
public class HRStatisticsDTO {
    private long totalApprovedJobs;
    private long totalRejectedJobs;
    private long totalPendingJobs;
    private long totalResumes;
    // Chi tiết job đang active và số resume
    private List<JobResumeStatistic> activeJobsWithResumes;

    // Thống kê resume theo status
    private List<ResumeStatusStatistic> resumesByStatus;

    // Thống kê theo thời gian (tuần hoặc tháng)
    private List<TimeSeriesStatistic> jobsTimeSeries;
    private List<TimeSeriesStatistic> resumesTimeSeries;

    // Metadata
    private Instant statisticsTime;
    private Instant filterStartDate;
    private Instant filterEndDate;
    private String timeUnit; // "WEEK" hoặc "MONTH"

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobResumeStatistic {
        private Long jobId;
        private String jobName;
        private long resumeCount;
        private Instant startDate;
        private Instant endDate;
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
        private String label; // "Week 1", "Week 2" hoặc "01/2025"
        private long count;
        private Instant periodStart;
        private Instant periodEnd;
    }
}
