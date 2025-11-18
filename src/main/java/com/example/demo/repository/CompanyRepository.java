package com.example.demo.repository;

import com.example.demo.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long>, JpaSpecificationExecutor<Company> {
    List<Company> findAllByHrId(Long id);
    // Lấy top companies theo resume count
    @Query(value = "SELECT c.id, c.name, c.logo, COUNT(r.id) as resume_count " +
            "FROM companies c " +
            "LEFT JOIN jobs j ON c.id = j.company_id AND j.status = 'APPROVED' " +
            "LEFT JOIN resumes r ON j.id = r.job_id " +
            "WHERE r.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name, c.logo " +
            "ORDER BY resume_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopCompaniesByResumeCount(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("limit") int limit
    );
}
