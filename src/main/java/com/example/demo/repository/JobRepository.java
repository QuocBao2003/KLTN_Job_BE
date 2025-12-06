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

    @Query("SELECT j from Job as j where j.jobProfession.id = :jobProfessionId and j.status=:jobStatus")
    Page<Job> findByJobProfessionIdAndStatus(@Param("jobProfessionId") Long jobProfessionId, @Param("jobStatus") JobStatus jobStatus, Pageable pageable);


    @Query ("SELECT DISTINCT j FROM Job as j where j.status = 'APPROVED'" +
            " AND UPPER(j.location) = UPPER(:location)" +
            "ORDER BY j.createdAt DESC ")
    List<Job> findByLocation(@Param("location") String location);

    @Query("""
        SELECT DISTINCT j FROM Job j
        INNER JOIN j.skills s
        WHERE j.status = 'APPROVED'
        AND LOWER(s.name) LIKE LOWER(CONCAT('%', :skillName, '%'))
        ORDER BY j.createdAt DESC
        """)
    List<Job> findBySkillName(@Param("skillName") String skillName);

    @Query("""
        SELECT DISTINCT j FROM Job j
        INNER JOIN j.jobProfession jp
        WHERE j.status = 'APPROVED'
        AND LOWER(jp.name) LIKE LOWER(CONCAT('%', :professionName, '%'))
        ORDER BY j.createdAt DESC
        """)
    List<Job> findByProfessionName(@Param("professionName") String professionName);

    @Query("""
        SELECT DISTINCT j FROM Job j
        LEFT JOIN j.skills s
        LEFT JOIN j.jobProfession jp
        WHERE j.status = 'APPROVED'
        AND (
            :location IS NULL OR UPPER(j.location) = UPPER(:location)
        )
        AND (
            :skillName IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :skillName, '%'))
        )
        AND (
            :professionName IS NULL OR LOWER(jp.name) LIKE LOWER(CONCAT('%', :professionName, '%'))
        )
        ORDER BY j.createdAt DESC
        """)
    List<Job> searchByMultipleCriteria(
            @Param("location") String location,
            @Param("skillName") String skillName,
            @Param("professionName") String professionName
    );
    List<Job> findTop10ByStatusOrderByCreatedAtDesc(JobStatus status);


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

    @Query("SELECT j FROM Job as j "+
        "LEFT JOIN j.userPackage as up " +
        "LEFT JOIN up.servicePackage as sp " +
        "WHERE j.status = :status "+
        "ORDER BY CASE "+
        "WHEN sp.packageType = 'FEATURED_JOB' THEN 1 "+
            "WHEN sp.packageType = 'PRIORITY_BOLD_TITLE' THEN 2 " +
            "WHEN sp.packageType = 'PRIORITY_DISPLAY' THEN 3 " +
            "ELSE 4 END, j.updatedAt DESC")
    Page<Job> findAllWithPackagePriority(@Param("status") JobStatus status, Pageable pageable);




}
