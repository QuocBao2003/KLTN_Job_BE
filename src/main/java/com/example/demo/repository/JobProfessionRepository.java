package com.example.demo.repository;

import com.example.demo.domain.JobProfession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

@Repository
public interface JobProfessionRepository extends JpaRepository<com.example.demo.domain.JobProfession,Long> {
    boolean existsByName(String name);

    Page<JobProfession> findAll(Specification<JobProfession> spec, Pageable pageable);

    @Query("SELECT DISTINCT p FROM JobProfession p LEFT JOIN FETCH p.jobs")
    List<JobProfession> findAllWithJobsAndSkills();
}
