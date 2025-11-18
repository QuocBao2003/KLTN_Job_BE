package com.example.demo.service;

import com.example.demo.domain.Company;
import com.example.demo.domain.JobProfession;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.JobProfessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;



@Service
public class JobProfessionService {
    private final JobProfessionRepository jobProfessionRepository;

    public JobProfessionService(JobProfessionRepository jobProfessionRepository) {
        this.jobProfessionRepository = jobProfessionRepository;
    }

    public JobProfession create(JobProfession profession){
        if(profession.getName() != null && jobProfessionRepository.existsByName(profession.getName())){
            throw new RuntimeException("JobProfession đã tồn tại");
        }
        return jobProfessionRepository.save(profession);
    }

    public JobProfession update(Long id, JobProfession profession) {
        JobProfession current = jobProfessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobProfession không tồn tại"));

        if (profession.getName() != null && !profession.getName().trim().isEmpty()) {
            // Kiểm tra tên mới có trùng với profession khác không
            if (!current.getName().equals(profession.getName())
                    && jobProfessionRepository.existsByName(profession.getName())) {
                throw new RuntimeException("Tên nghề nghiệp đã tồn tại");
            }
            current.setName(profession.getName());
        }

        return jobProfessionRepository.save(current);
    }

    public void delete(Long id) {
        JobProfession current = jobProfessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobProfession không tồn tại"));

        if (current.getSkills() != null && !current.getSkills().isEmpty()) {
            throw new RuntimeException("Không thể xóa, còn " + current.getSkills().size() + " skill liên quan");
        }

        if (current.getJobs() != null && !current.getJobs().isEmpty()) {
            throw new RuntimeException("Không thể xóa, còn " + current.getJobs().size() + " job liên quan");
        }

        jobProfessionRepository.delete(current);
    }
    public JobProfession getById(Long id) {
        return jobProfessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobProfession không tồn tại"));
    }
    public ResultPaginationDTO getAll(Specification<JobProfession> spec, Pageable pageable){
        Page<JobProfession> jobProfessions=jobProfessionRepository.findAll(spec,pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(jobProfessions.getNumber()+1);
        mt.setPageSize(jobProfessions.getSize());
        mt.setPages(jobProfessions.getTotalPages());
        mt.setTotal(jobProfessions.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(jobProfessions.getContent());
        return rs;
    }

}
