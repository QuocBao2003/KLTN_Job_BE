package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.request.AdminStatisticsDTO;
import com.example.demo.dto.request.HRStatisticsDTO;
import com.example.demo.dto.request.StatisticsFilterDTO;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.Enum.ResumeStateEnum;
import com.example.demo.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public StatisticsService(JobRepository jobRepository,
                             ResumeRepository resumeRepository,
                             CompanyRepository companyRepository,
                             UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Thống kê cho HR
     */
    public HRStatisticsDTO getHRStatistics(StatisticsFilterDTO filter) {
        filter.validate();

        // Lấy thông tin HR
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User hrUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!hrUser.getRole().getName().equalsIgnoreCase("HR")) {
            throw new RuntimeException("User is not HR");
        }

        HRStatisticsDTO statistics = new HRStatisticsDTO();

        // ✅ KIỂM TRA: Nếu HR chưa có company, trả về thống kê = 0
        if (hrUser.getCompany() == null) {
            return buildEmptyHRStatistics(filter);
        }

        Long companyId = hrUser.getCompany().getId();

        // 1. Thống kê job theo status
        statistics.setTotalApprovedJobs(
                jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.APPROVED)
        );
        statistics.setTotalRejectedJobs(
                jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.REJECTED)
        );
        statistics.setTotalPendingJobs(
                jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.PENDING)
        );

        // 2. Thống kê tổng số resume
        statistics.setTotalResumes(
                resumeRepository.countByJobCompanyId(companyId)
        );

        // 3. Thống kê job đang active và số resume
        List<Map<String, Object>> jobSummaries = jobRepository
                .findJobSummaryByCompanyIdAndStatus(companyId, JobStatus.APPROVED);

        List<Long> jobIds = jobSummaries.stream()
                .map(map -> ((Number) map.get("id")).longValue())
                .collect(Collectors.toList());

        Map<Long, Long> resumeCountMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            List<Object[]> resumeCounts = resumeRepository.countResumesByJobIds(jobIds);
            for (Object[] row : resumeCounts) {
                resumeCountMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }

        List<HRStatisticsDTO.JobResumeStatistic> jobStats = jobSummaries.stream()
                .map(map -> {
                    Long jobId = ((Number) map.get("id")).longValue();
                    return new HRStatisticsDTO.JobResumeStatistic(
                            jobId,
                            (String) map.get("name"),
                            resumeCountMap.getOrDefault(jobId, 0L),
                            (Instant) map.get("startDate"),
                            (Instant) map.get("endDate")
                    );
                })
                .sorted((a, b) -> Long.compare(b.getResumeCount(), a.getResumeCount()))
                .collect(Collectors.toList());
        statistics.setActiveJobsWithResumes(jobStats);

        // 4. Thống kê resume theo status
        List<Object[]> resumeStatusData = resumeRepository.countResumesByStatusAndCompany(companyId);
        List<HRStatisticsDTO.ResumeStatusStatistic> resumeStatusStats = resumeStatusData.stream()
                .map(row -> new HRStatisticsDTO.ResumeStatusStatistic(
                        row[0] != null ? row[0].toString() : "PENDING",
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        statistics.setResumesByStatus(resumeStatusStats);

        // 5. Thống kê theo thời gian
        if ("WEEK".equalsIgnoreCase(filter.getTimeUnit())) {
            statistics.setJobsTimeSeries(
                    buildWeeklyTimeSeriesForHR(
                            jobRepository.countJobsByWeekAndCompany(companyId, filter.getStartDate(), filter.getEndDate())
                    )
            );
            statistics.setResumesTimeSeries(
                    buildWeeklyTimeSeriesForHR(
                            resumeRepository.countResumesByWeekAndCompany(companyId, filter.getStartDate(), filter.getEndDate())
                    )
            );
        } else {
            statistics.setJobsTimeSeries(
                    buildMonthlyTimeSeriesForHR(
                            jobRepository.countJobsByMonthAndCompany(companyId, filter.getStartDate(), filter.getEndDate())
                    )
            );
            statistics.setResumesTimeSeries(
                    buildMonthlyTimeSeriesForHR(
                            resumeRepository.countResumesByMonthAndCompany(companyId, filter.getStartDate(), filter.getEndDate())
                    )
            );
        }

        // 6. Set metadata
        statistics.setStatisticsTime(Instant.now());
        statistics.setFilterStartDate(filter.getStartDate());
        statistics.setFilterEndDate(filter.getEndDate());
        statistics.setTimeUnit(filter.getTimeUnit());

        return statistics;
    }

    /**
     * Thống kê cho Admin
     */
    public AdminStatisticsDTO getAdminStatistics(StatisticsFilterDTO filter) {
        filter.validate();

        // Kiểm tra quyền Admin
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User adminUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!adminUser.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new RuntimeException("User is not Admin");
        }

        AdminStatisticsDTO statistics = new AdminStatisticsDTO();

        // 1. Thống kê job toàn hệ thống
        statistics.setTotalApprovedJobs(jobRepository.countByStatus(JobStatus.APPROVED));
        statistics.setTotalRejectedJobs(jobRepository.countByStatus(JobStatus.REJECTED));
        statistics.setTotalPendingJobs(jobRepository.countByStatus(JobStatus.PENDING));

        // 2. Thống kê tổng số company
        statistics.setTotalCompanies(companyRepository.count());

        // 3. Company có nhiều resume nhất
        List<Object[]> topCompaniesData = companyRepository
                .findTopCompaniesByResumeCount(filter.getStartDate(), filter.getEndDate(), 1);

        if (!topCompaniesData.isEmpty()) {
            Object[] topCompany = topCompaniesData.get(0);
            statistics.setTopCompanyByResumes(
                    new AdminStatisticsDTO.CompanyTopResume(
                            ((Number) topCompany[0]).longValue(),
                            (String) topCompany[1],
                            (String) topCompany[2],
                            ((Number) topCompany[3]).longValue()
                    )
            );
        }

        // 4. Thống kê resume theo status
        List<Object[]> resumeStatusData = resumeRepository.countResumesByStatus();
        List<AdminStatisticsDTO.ResumeStatusStatistic> resumeStatusStats = resumeStatusData.stream()
                .map(row -> new AdminStatisticsDTO.ResumeStatusStatistic(
                        row[0] != null ? row[0].toString() : "PENDING",
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        statistics.setResumesByStatus(resumeStatusStats);

        // 5. Thống kê theo thời gian
        if ("WEEK".equalsIgnoreCase(filter.getTimeUnit())) {
            statistics.setJobsTimeSeries(
                    buildWeeklyTimeSeriesForAdmin(
                            jobRepository.countJobsByWeek(filter.getStartDate(), filter.getEndDate())
                    )
            );
            statistics.setResumesTimeSeries(
                    buildWeeklyTimeSeriesForAdmin(
                            resumeRepository.countResumesByWeek(filter.getStartDate(), filter.getEndDate())
                    )
            );
        } else {
            statistics.setJobsTimeSeries(
                    buildMonthlyTimeSeriesForAdmin(
                            jobRepository.countJobsByMonth(filter.getStartDate(), filter.getEndDate())
                    )
            );
            statistics.setResumesTimeSeries(
                    buildMonthlyTimeSeriesForAdmin(
                            resumeRepository.countResumesByMonth(filter.getStartDate(), filter.getEndDate())
                    )
            );
        }

        // 6. Top companies theo resume
        List<Object[]> topCompanies = companyRepository
                .findTopCompaniesByResumeCount(filter.getStartDate(), filter.getEndDate(), filter.getTopLimit());

        List<AdminStatisticsDTO.CompanyResumeStatistic> companyStats = topCompanies.stream()
                .map(row -> {
                    Long companyId = ((Number) row[0]).longValue();
                    String companyName = (String) row[1];
                    long totalResumes = ((Number) row[3]).longValue();

                    long approvedResumes = resumeRepository.countByJobCompanyIdAndStatus(
                            companyId, ResumeStateEnum.APPROVED
                    );
                    long rejectedResumes = resumeRepository.countByJobCompanyIdAndStatus(
                            companyId, ResumeStateEnum.REJECTED
                    );
                    long pendingResumes = resumeRepository.countByJobCompanyIdAndStatus(
                            companyId, ResumeStateEnum.PENDING
                    );

                    return new AdminStatisticsDTO.CompanyResumeStatistic(
                            companyId,
                            companyName,
                            totalResumes,
                            approvedResumes,
                            rejectedResumes,
                            pendingResumes
                    );
                })
                .collect(Collectors.toList());
        statistics.setCompanyResumeStatistics(companyStats);

        // 7. Set metadata
        statistics.setStatisticsTime(Instant.now());
        statistics.setFilterStartDate(filter.getStartDate());
        statistics.setFilterEndDate(filter.getEndDate());
        statistics.setTimeUnit(filter.getTimeUnit());

        return statistics;
    }

    /**
     * ✅ Tạo thống kê rỗng cho HR chưa có company
     */
    private HRStatisticsDTO buildEmptyHRStatistics(StatisticsFilterDTO filter) {
        HRStatisticsDTO statistics = new HRStatisticsDTO();

        // Set tất cả = 0
        statistics.setTotalApprovedJobs(0);
        statistics.setTotalRejectedJobs(0);
        statistics.setTotalPendingJobs(0);
        statistics.setTotalResumes(0);

        // Empty lists
        statistics.setActiveJobsWithResumes(new ArrayList<>());
        statistics.setResumesByStatus(new ArrayList<>());
        statistics.setJobsTimeSeries(new ArrayList<>());
        statistics.setResumesTimeSeries(new ArrayList<>());

        // Set metadata
        statistics.setStatisticsTime(Instant.now());
        statistics.setFilterStartDate(filter.getStartDate());
        statistics.setFilterEndDate(filter.getEndDate());
        statistics.setTimeUnit(filter.getTimeUnit());

        return statistics;
    }

    /**
     * Build monthly time series cho HR
     */
    private List<HRStatisticsDTO.TimeSeriesStatistic> buildMonthlyTimeSeriesForHR(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int month = ((Number) row[1]).intValue();
                    long count = ((Number) row[2]).longValue();
                    String label = String.format("%02d/%d", month, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, month, 1, 0, 0);
                    LocalDateTime periodEnd = periodStart.plusMonths(1).minusSeconds(1);

                    return new HRStatisticsDTO.TimeSeriesStatistic(
                            label,
                            count,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Build weekly time series cho HR
     */
    private List<HRStatisticsDTO.TimeSeriesStatistic> buildWeeklyTimeSeriesForHR(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int week = ((Number) row[1]).intValue();
                    long count = ((Number) row[2]).longValue();
                    String label = String.format("Week %d-%d", week, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, 1, 1, 0, 0).plusWeeks(week - 1);
                    LocalDateTime periodEnd = periodStart.plusWeeks(1).minusSeconds(1);

                    return new HRStatisticsDTO.TimeSeriesStatistic(
                            label,
                            count,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Build monthly time series cho Admin
     */
    private List<AdminStatisticsDTO.TimeSeriesStatistic> buildMonthlyTimeSeriesForAdmin(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int month = ((Number) row[1]).intValue();
                    long count = ((Number) row[2]).longValue();
                    String label = String.format("%02d/%d", month, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, month, 1, 0, 0);
                    LocalDateTime periodEnd = periodStart.plusMonths(1).minusSeconds(1);

                    return new AdminStatisticsDTO.TimeSeriesStatistic(
                            label,
                            count,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Build weekly time series cho Admin
     */
    private List<AdminStatisticsDTO.TimeSeriesStatistic> buildWeeklyTimeSeriesForAdmin(List<Object[]> data) {
        return data.stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int week = ((Number) row[1]).intValue();
                    long count = ((Number) row[2]).longValue();
                    String label = String.format("Week %d-%d", week, year);

                    LocalDateTime periodStart = LocalDateTime.of(year, 1, 1, 0, 0).plusWeeks(week - 1);
                    LocalDateTime periodEnd = periodStart.plusWeeks(1).minusSeconds(1);

                    return new AdminStatisticsDTO.TimeSeriesStatistic(
                            label,
                            count,
                            periodStart.atZone(ZoneId.systemDefault()).toInstant(),
                            periodEnd.atZone(ZoneId.systemDefault()).toInstant()
                    );
                })
                .collect(Collectors.toList());
    }
}

