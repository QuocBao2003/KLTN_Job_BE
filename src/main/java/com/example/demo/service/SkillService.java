package com.example.demo.service;

import com.example.demo.domain.JobProfession;
import com.example.demo.domain.Skill;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.JobProfessionRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {
    private final SkillRepository skillRepository;
    private final JobProfessionRepository jobProfessionRepository;

    public SkillService(SkillRepository skillRepository, JobProfessionRepository jobProfessionRepository) {
        this.skillRepository = skillRepository;
        this.jobProfessionRepository = jobProfessionRepository;
    }

    // Create Skill
    public Skill create(Skill skill, Long professionId) {
        JobProfession profession = jobProfessionRepository.findById(professionId)
                .orElseThrow(() -> new RuntimeException("JobProfession không tồn tại"));

        // Set profession
        skill.setJobProfession(profession);

        // Kiểm tra tên trùng trong profession
        if(skillRepository.existsByNameAndJobProfession_Id(skill.getName(), professionId)) {
            throw new RuntimeException("Skill đã tồn tại trong nghề nghiệp này");
        }

        return skillRepository.save(skill);
    }
    public boolean isNameExist(String name) {
        return skillRepository.existsByNameIgnoreCase(name);
    }
    // Update Skill
    public Skill update(Long skillId, Skill skill) {
        Skill current = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill không tồn tại"));

        if(skill.getName() != null) current.setName(skill.getName());
        return skillRepository.save(current);
    }

    // Delete Skill
    public void delete(Long skillId) {
        Skill current = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill không tồn tại"));

        // Remove associations with Jobs
        if(current.getJobs() != null) {
            current.getJobs().forEach(job -> job.getSkills().remove(current));
        }

        skillRepository.delete(current);
    }

    // Get by Id
    public Skill getById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill không tồn tại"));
    }

    // Get all Skills (paginate + filter)
    public ResultPaginationDTO getAll(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> page = skillRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setTotal(page.getTotalElements());
        mt.setPages(page.getTotalPages());
        rs.setMeta(mt);
        rs.setResult(page.getContent());
        return rs;
    }
    public List<Skill> getByProfessionId(Long professionId) {

        jobProfessionRepository.findById(professionId)
                .orElseThrow(() -> new RuntimeException("JobProfession không tồn tại"));

        return skillRepository.findAllByJobProfession_Id(professionId);
    }



}

