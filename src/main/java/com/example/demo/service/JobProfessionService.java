package com.example.demo.service;

import com.example.demo.domain.Company;
import com.example.demo.domain.Job;
import com.example.demo.domain.JobProfession;
import com.example.demo.dto.JobFilterDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.JobProfessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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

    @Transactional(readOnly = true)
    public List<JobFilterDTO.ProfessionResponse> getTreeMenuData(String keyword) {
        // 1. Lấy TOÀN BỘ dữ liệu (Vẫn dùng hàm fetch tối ưu cũ)
        List<JobProfession> entities = jobProfessionRepository.findAllWithJobsAndSkills();

        // 2. Xử lý chuỗi tìm kiếm (chuyển về chữ thường để so sánh tương đối)
        String finalKeyword = (keyword == null) ? "" : keyword.toLowerCase().trim();

        // 3. Lọc và Map dữ liệu (Stream API)
        return entities.stream()
                .map(profession -> convertToDtoWithFilter(profession, finalKeyword))
                .filter(dto -> dto != null) // Loại bỏ các Profession không thỏa mãn điều kiện
                .collect(Collectors.toList());
    }

    // Hàm để lọc đệ quy từ Cha -> Con -> Cháu
    private JobFilterDTO.ProfessionResponse convertToDtoWithFilter(JobProfession profession, String keyword) {
        // Bước 1: Lọc các Job con
        List<JobFilterDTO.JobResponse> validJobs = new ArrayList<>();

        if (profession.getJobs() != null) {
            for (Job job : profession.getJobs()) {
                JobFilterDTO.JobResponse jobDto = convertJobWithFilter(job, keyword);
                if (jobDto != null) {
                    validJobs.add(jobDto);
                }
            }
        }

        // Bước 2: Kiểm tra logic giữ lại Profession
        // Giữ lại nểu: Tên Profession khớp keyword HOẶC có Job con thỏa mãn
        boolean isMatchName = profession.getName().toLowerCase().contains(keyword);
        boolean hasChild = !validJobs.isEmpty();

        if (isMatchName || hasChild) {
            // Nếu Profession khớp tên -> hiển thị toàn bộ Job con (hoặc tùy logic bạn muốn chỉ hiện job lọc)
            // Ở đây tôi để logic: Nếu cha khớp thì vẫn hiển thị các con đã lọc (clean hơn)
            return new JobFilterDTO.ProfessionResponse(profession.getId(), profession.getName(), validJobs);
        }

        return null; // Bỏ qua Profession này
    }

    private JobFilterDTO.JobResponse convertJobWithFilter(Job job, String keyword) {
        // Bước 1: Lọc các Skill con
        List<JobFilterDTO.SkillResponse> validSkills = new ArrayList<>();

        if (job.getSkills() != null) {
            validSkills = job.getSkills().stream()
                    .filter(skill -> skill.getName().toLowerCase().contains(keyword)
                            || job.getName().toLowerCase().contains(keyword) // Nếu Job khớp thì lấy hết skill
                            || keyword.isEmpty())
                    .map(skill -> new JobFilterDTO.SkillResponse(skill.getId(), skill.getName()))
                    .collect(Collectors.toList());
        }

        // Bước 2: Kiểm tra logic giữ lại Job
        boolean isMatchName = job.getName().toLowerCase().contains(keyword);
        boolean hasChild = !validSkills.isEmpty();

        // Nếu keyword rỗng -> lấy hết
        if (keyword.isEmpty()) return new JobFilterDTO.JobResponse(job.getId(), job.getName(), validSkills);

        if (isMatchName || hasChild) {
            return new JobFilterDTO.JobResponse(job.getId(), job.getName(), validSkills);
        }

        return null; // Bỏ qua Job này
    }
}
