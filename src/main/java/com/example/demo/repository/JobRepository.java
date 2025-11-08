package com.example.demo.repository;

import com.example.demo.domain.Job;
import com.example.demo.domain.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    boolean existsJobByName(String name);

    Page<Job> findAll(Specification<Job> spec, Pageable pageable);
//    tìm danh sách công việc chứa skill
    List<Job> findBySkillsIn(List<Skill> skills);

    @Query("""
        SELECT DISTINCT j FROM Job j 
        LEFT JOIN j.skills s 
        WHERE j.status = 'ACTIVE' 
          AND (LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
               OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY j.createdAt DESC
    """)
    List<Job> findByNameOrSkillContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("""
        SELECT DISTINCT j FROM Job j
        LEFT JOIN j.skills s
        WHERE LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY j.id DESC
        """)
    List<Job> searchJobs(String keyword);
}
