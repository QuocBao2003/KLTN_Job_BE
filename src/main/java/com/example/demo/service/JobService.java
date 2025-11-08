package com.example.demo.service;

import com.example.demo.domain.Company;
import com.example.demo.domain.Job;
import com.example.demo.domain.Skill;
import com.example.demo.domain.User;
import com.example.demo.dto.response.job.ResCreateJobDTO;
import com.example.demo.dto.response.job.ResUpdateJobDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    public JobService(JobRepository jobRepository, SkillRepository skillRepository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public ResCreateJobDTO createJob(Job job) {
        //check user
        String currentLogin = SecurityUtil.getCurrentUserLogin().orElseThrow(()-> new RuntimeException("User not found"));
        User hrUser =  userRepository.findByEmail(currentLogin).orElseThrow(()-> new RuntimeException("User not found"));
        if(!hrUser.getRole().getName().equals("HR")){
            throw new RuntimeException("User is not HR for Company");
        }
        job.setStatus(JobStatus.PENDING);
        job.setCompany(hrUser.getCompany());


//        ckeck skill
        if (job.getSkills() != null) {
            List<Long> reqskills = job.getSkills().stream()
                    .map(x -> x.getId())
                    .collect(Collectors.toList());
//            danh sách skill tônf tại dưới data
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqskills);
            job.setSkills(dbSkills);

            }

//          create job
            Job currentJob = this.jobRepository.save(job);
            ResCreateJobDTO rs = new ResCreateJobDTO();
            rs.setId(currentJob.getId());
            rs.setQuantity(currentJob.getQuantity());
            rs.setName(currentJob.getName());
            rs.setLocation(currentJob.getLocation());
            rs.setSalary(currentJob.getSalary());
            rs.setLevel(currentJob.getLevel());
            rs.setStartDate(currentJob.getStartDate());
            rs.setEndDate(currentJob.getEndDate());
            rs.setStatus(currentJob.getStatus());
            rs.setCreatedAt(currentJob.getCreatedAt());
            rs.setCreatedBy(currentJob.getCreatedBy());
            if (currentJob.getSkills() != null) {
                List<String> skill = currentJob.getSkills().stream()
                        .map(x -> x.getName()).collect(Collectors.toList());
                rs.setSkills(skill);
            }
            return rs;

        }
        public Optional<Job> getJobById(long id){
        return this.jobRepository.findById(id);
        }

        public ResUpdateJobDTO updateJob(Job job,Job jobInDB) {

            if (job.getSkills() != null) {
                List<Long> reqskills = job.getSkills().stream()
                        .map(x -> x.getId())
                        .collect(Collectors.toList());
//            danh sách skill tônf tại dưới data
                List<Skill> dbSkills = this.skillRepository.findByIdIn(reqskills);
                jobInDB.setSkills(dbSkills);

            }
//          check company
            if(job.getCompany()!=null){
                Optional<Company> companyOptional=this.companyRepository.findById(job.getCompany().getId());
                if(companyOptional.isPresent()){
                    jobInDB.setCompany(companyOptional.get());
                }
            }
//          update correct info
            jobInDB.setName(job.getName());
            jobInDB.setSalary(job.getSalary());
            jobInDB.setQuantity(job.getQuantity());
            jobInDB.setLocation(job.getLocation());
            jobInDB.setLevel(job.getLevel());
            jobInDB.setStartDate(job.getStartDate());
            jobInDB.setEndDate(job.getEndDate());

//          update job
            Job currentJob = this.jobRepository.save(jobInDB);
//          convert response

           ResUpdateJobDTO rs = new ResUpdateJobDTO();
            rs.setId(currentJob.getId());
            rs.setQuantity(currentJob.getQuantity());
            rs.setName(currentJob.getName());
            rs.setLocation(currentJob.getLocation());
            rs.setSalary(currentJob.getSalary());
            rs.setLevel(currentJob.getLevel());
            rs.setStartDate(currentJob.getStartDate());
            rs.setEndDate(currentJob.getEndDate());

            rs.setUpdatedAt(currentJob.getUpdatedAt());
            rs.setUpdatedBy(currentJob.getUpdatedBy());
            if (currentJob.getSkills() != null) {
                List<String> skill = currentJob.getSkills().stream()
                        .map(x -> x.getName()).collect(Collectors.toList());
                rs.setSkills(skill);
            }
            return rs;

        }
        public void deleteJob(Long id){
            jobRepository.deleteById(id);
        }

        public ResultPaginationDTO getAllJob(Specification<Job> spec, Pageable pageable) {
            Page<Job> pagejob = this.jobRepository.findAll(spec,pageable);
            ResultPaginationDTO rs = new ResultPaginationDTO();
            ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
            mt.setPage(pageable.getPageNumber()+1);
            mt.setPageSize(pageable.getPageSize());
            mt.setTotal(pagejob.getTotalElements());
            mt.setPages(pagejob.getTotalPages());
            rs.setMeta(mt);
            rs.setResult(pagejob.getContent());
            return  rs;
        }

        public void approveJob(Long id){
        Job job = this.jobRepository.findById(id).orElseThrow(()-> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.APPROVED);
        this.jobRepository.save(job);
        }

        public void rejectJob(Long id){
        Job job = this.jobRepository.findById(id).orElseThrow(()-> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.REJECTED);
        this.jobRepository.save(job);
        }
    }


