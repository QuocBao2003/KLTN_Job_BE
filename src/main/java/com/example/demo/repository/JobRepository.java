package com.example.demo.repository;

import com.example.demo.domain.Job;
import com.example.demo.domain.Skill;
import com.example.demo.dto.JobStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobRepository extends JpaRepository<Job,Long> {
    boolean existsJobByName(String name);

    Page<Job> findAll(Specification<Job> spec, Pageable pageable);
//    tìm danh sách công việc chứa skill
    List<Job> findBySkillsIn(List<Skill> skills);

    @Query("SELECT j.level, COUNT(j), AVG(j.salary) " +
            "FROM Job j WHERE j.active = true GROUP BY j.level")
    List<Object[]> getJobStatisticsByLevel();

    @Query("SELECT j.location, COUNT(j), AVG(j.salary) " +
            "FROM Job j WHERE j.active = true GROUP BY j.location")
    List<Object[]> getJobStatisticsByLocation();

    @Query("SELECT j.company.name, COUNT(j), AVG(j.salary) " +
            "FROM Job j WHERE j.active = true GROUP BY j.company.name")
    List<Object[]> getJobStatisticsByCompany();
}
