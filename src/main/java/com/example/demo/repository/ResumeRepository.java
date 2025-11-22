package com.example.demo.repository;

import com.example.demo.domain.Resume;
import com.example.demo.util.Enum.ResumeStateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findAll(Specification<Resume> spec, Pageable pageable);
    @Query("""
        SELECT r FROM Resume r
                                JOIN FETCH r.job j
                                JOIN FETCH j.company c
                                LEFT JOIN FETCH c.hr hr
                                LEFT JOIN FETCH hr.role
                                JOIN FETCH r.user
                                WHERE r.id = :id
    """)
    Optional<Resume> findWithRelationsById(@Param("id") Long id);
    // Đếm resume theo job
    long countByJobId(Long jobId);
    @Query("SELECT COUNT(r) FROM Resume r WHERE r.job.company.id = :companyId")
    long countByJobCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(r) FROM Resume r WHERE r.job.company.id = :companyId AND r.status = :status")
    long countByJobCompanyIdAndStatus(@Param("companyId") Long companyId,
                                      @Param("status") ResumeStateEnum status);



    @Query("SELECT r.job.id as jobId, COUNT(r) as count " +
            "FROM Resume r WHERE r.job.id IN :jobIds GROUP BY r.job.id")
    List<Object[]> countResumesByJobIds(@Param("jobIds") List<Long> jobIds);

    // Đếm resume theo status - toàn hệ thống
    @Query("SELECT r.status as status, COUNT(r) as count FROM Resume r GROUP BY r.status")
    List<Object[]> countResumesByStatus();

    // Đếm resume theo status - theo company
    @Query("SELECT r.status as status, COUNT(r) as count " +
            "FROM Resume r WHERE r.job.company.id = :companyId GROUP BY r.status")
    List<Object[]> countResumesByStatusAndCompany(@Param("companyId") Long companyId);

    // Thống kê resume theo tháng - toàn hệ thống
    @Query(value = "SELECT YEAR(created_at) as year, MONTH(created_at) as month, COUNT(*) as count " +
            "FROM resumes " +
            "WHERE created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> countResumesByMonth(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê resume theo tháng - theo company
    @Query(value = "SELECT YEAR(r.created_at) as year, MONTH(r.created_at) as month, COUNT(*) as count " +
            "FROM resumes r " +
            "JOIN jobs j ON r.job_id = j.id " +
            "WHERE j.company_id = :companyId " +
            "AND r.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(r.created_at), MONTH(r.created_at) " +
            "ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> countResumesByMonthAndCompany(
            @Param("companyId") Long companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê resume theo tuần - toàn hệ thống
    @Query(value = "SELECT YEAR(created_at) as year, WEEK(created_at) as week, COUNT(*) as count " +
            "FROM resumes " +
            "WHERE created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(created_at), WEEK(created_at) " +
            "ORDER BY year, week",
            nativeQuery = true)
    List<Object[]> countResumesByWeek(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Thống kê resume theo tuần - theo company
    @Query(value = "SELECT YEAR(r.created_at) as year, WEEK(r.created_at) as week, COUNT(*) as count " +
            "FROM resumes r " +
            "JOIN jobs j ON r.job_id = j.id " +
            "WHERE j.company_id = :companyId " +
            "AND r.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(r.created_at), WEEK(r.created_at) " +
            "ORDER BY year, week",
            nativeQuery = true)
    List<Object[]> countResumesByWeekAndCompany(
            @Param("companyId") Long companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


}
