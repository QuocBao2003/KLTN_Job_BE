package com.example.demo.repository;

import com.example.demo.domain.Job;
import com.example.demo.domain.Skill;
import com.example.demo.util.Enum.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    boolean existsJobByName(String name);

    Page<Job> findAll(Specification<Job> spec, Pageable pageable);
//    tìm danh sách công việc chứa skill
    List<Job> findBySkillsIn(List<Skill> skills);


    @Query("""
        SELECT DISTINCT j FROM Job j
        LEFT JOIN j.skills s
        WHERE LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY j.id DESC
        """)
    List<Job> searchJobs(String keyword);

   Page<Job> findByCompanyIdAndStatus(Long companyId, JobStatus status, Pageable pageable);
// đếm job them company and status
   long countByCompanyIdAndStatus(Long companyId, JobStatus status);


    // Đếm job theo status (toàn hệ thống)
    long countByStatus(JobStatus status);

    // Lấy job summary để tránh load full entity
    @Query("SELECT new map(j.id as id, j.name as name, j.startDate as startDate, j.endDate as endDate) " +
            "FROM Job j WHERE j.company.id = :companyId AND j.status = :status")
    List<Map<String, Object>> findJobSummaryByCompanyIdAndStatus(
            @Param("companyId") Long companyId,
            @Param("status") JobStatus status
    );
    // Thống kê job theo tháng - toàn hệ thống
    @Query(value = "SELECT YEAR(created_at) as year, MONTH(created_at) as month, COUNT(*) as count " +
            "FROM jobs " +
            "WHERE status = 'APPROVED' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> countJobsByMonth(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê job theo tháng - theo company
    @Query(value = "SELECT YEAR(created_at) as year, MONTH(created_at) as month, COUNT(*) as count " +
            "FROM jobs " +
            "WHERE company_id = :companyId " +
            "AND status = 'APPROVED' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> countJobsByMonthAndCompany(
            @Param("companyId") Long companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê job theo tuần - toàn hệ thống
    @Query(value = "SELECT YEAR(created_at) as year, WEEK(created_at) as week, COUNT(*) as count " +
            "FROM jobs " +
            "WHERE status = 'APPROVED' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), WEEK(created_at) " +
            "ORDER BY year, week",
            nativeQuery = true)
    List<Object[]> countJobsByWeek(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê job theo tuần - theo company
    @Query(value = "SELECT YEAR(created_at) as year, WEEK(created_at) as week, COUNT(*) as count " +
            "FROM jobs " +
            "WHERE company_id = :companyId " +
            "AND status = 'APPROVED' " +
            "AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), WEEK(created_at) " +
            "ORDER BY year, week",
            nativeQuery = true)
    List<Object[]> countJobsByWeekAndCompany(
            @Param("companyId") Long companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );




}
