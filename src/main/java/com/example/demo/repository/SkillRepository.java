package com.example.demo.repository;

import com.example.demo.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SkillRepository extends JpaRepository<Skill,Long>, JpaSpecificationExecutor<Skill> {
    boolean existsByNameAndJobProfession_Id(String name, Long professionId);
    List<Skill> findByIdIn(List<Long> ids);
    boolean existsByNameIgnoreCase(String name);
    List<Skill> findAllByJobProfession_Id(Long id);


}
