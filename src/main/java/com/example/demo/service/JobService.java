package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.response.job.ResCreateJobDTO;
import com.example.demo.dto.response.job.ResUpdateJobDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.*;
import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.Enum.PackageStatus;
import com.example.demo.util.Enum.PackageType;
import com.example.demo.util.Enum.SalaryTypeEnum;
import com.example.demo.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserPackageRepository userPackageRepository;
    private final UserPackageService userPackageService;
    private final JobProfessionRepository jobProfessionRepository;
    public JobService(JobRepository jobRepository, SkillRepository skillRepository,
                      CompanyRepository companyRepository, UserRepository userRepository,
                      NotificationService notificationService, UserPackageRepository userPackageRepository, UserPackageService userPackageService, JobProfessionRepository jobProfessionRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.userPackageRepository = userPackageRepository;
        this.userPackageService = userPackageService;
        this.jobProfessionRepository = jobProfessionRepository;
    }

    public ResCreateJobDTO createJob(Job job,Long userPackageId) {
        // Check user
        String currentLogin = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));
        User hrUser = userRepository.findByEmail(currentLogin)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!hrUser.getRole().getName().equals("HR")) {
            throw new RuntimeException("User is not HR for Company");
        }
        if (userPackageId == null) {
            throw new RuntimeException("User package is required to create job");
        }

        UserPackage userPackage = userPackageRepository.findById(userPackageId)
                .orElseThrow(() -> new RuntimeException("User package not found"));

        // Validate user package
        if (!userPackage.getUser().getId().equals(hrUser.getId())) {
            throw new RuntimeException("User package does not belong to current user");
        }

        if (userPackage.getStatus() != PackageStatus.ACTIVE) {
            throw new RuntimeException("User package is not active");
        }

        if (userPackage.getEndDate().isBefore(Instant.now())) {
            throw new RuntimeException("User package has expired");
        }

        if (userPackage.getRemainingJobCount() <= 0) {
            throw new RuntimeException("No remaining jobs in this package");
        }
        job.setUserPackage(userPackage);
        job.setStatus(JobStatus.PENDING);
        if (job.getCompany() != null && job.getCompany().getId() != null) {
            Company company = companyRepository.findById(job.getCompany().getId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            if (!company.getHr().equals(hrUser)) {
                throw new RuntimeException("User is not HR for the selected Company");
            }

            job.setCompany(company);
        } else {
            throw new RuntimeException("Company must be selected");
        }
        // Check JobProfession
        if (job.getJobProfession() != null && job.getJobProfession().getId() != null) {
            JobProfession profession = jobProfessionRepository.findById(job.getJobProfession().getId())
                    .orElseThrow(() -> new RuntimeException("JobProfession not found"));
            job.setJobProfession(profession);
        }

        // Check skills - chỉ lấy skills thuộc về JobProfession đã chọn
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            List<Long> reqSkillIds = job.getSkills().stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());

            List<Skill> dbSkills = skillRepository.findByIdIn(reqSkillIds);

            // Validate: Skills phải thuộc về JobProfession đã chọn
            if (job.getJobProfession() != null) {
                Long professionId = job.getJobProfession().getId();
                boolean allSkillsValid = dbSkills.stream()
                        .allMatch(skill -> skill.getJobProfession() != null
                                && skill.getJobProfession().getId().equals(professionId));

                if (!allSkillsValid) {
                    throw new RuntimeException("Some skills do not belong to the selected JobProfession");
                }
            }

            job.setSkills(dbSkills);
        }

        // Validate salary
        if (job.getSalaryType() == SalaryTypeEnum.SPECIFIC) {
            if (job.getMinSalary() == null || job.getMaxSalary() == null) {
                throw new RuntimeException("Min and Max salary are required for SPECIFIC salary type");
            }
            if (job.getMinSalary() > job.getMaxSalary()) {
                throw new RuntimeException("Min salary cannot be greater than Max salary");
            }
        } else if (job.getSalaryType() == SalaryTypeEnum.NEGOTIABLE) {
            // Nếu là thương lượng, set null cho min/max
            job.setMinSalary(null);
            job.setMaxSalary(null);
        }
        job.setDescription(job.getDescription());
        job.setRequest(job.getRequest());
        job.setInterest(job.getInterest());
        job.setWorklocation(job.getWorklocation());
        job.setWorktime(job.getWorktime());


        // Create job
        Job currentJob = jobRepository.save(job);
        userPackageService.decrementJobCount(userPackageId);
        notificationService.notificationJobCreate(currentJob);

        return convertToResCreateJobDTO(currentJob);
    }

    public ResUpdateJobDTO updateJob(Job job, Job jobInDB) {
        // Check JobProfession
        if (job.getJobProfession() != null && job.getJobProfession().getId() != null) {
            JobProfession profession = jobProfessionRepository.findById(job.getJobProfession().getId())
                    .orElseThrow(() -> new RuntimeException("JobProfession not found"));
            jobInDB.setJobProfession(profession);
        }

        // Check skills - chỉ lấy skills thuộc về JobProfession đã chọn
        if (job.getSkills() != null) {
            List<Long> reqSkillIds = job.getSkills().stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());

            List<Skill> dbSkills = skillRepository.findByIdIn(reqSkillIds);

            // Validate: Skills phải thuộc về JobProfession đã chọn
            if (jobInDB.getJobProfession() != null) {
                Long professionId = jobInDB.getJobProfession().getId();
                boolean allSkillsValid = dbSkills.stream()
                        .allMatch(skill -> skill.getJobProfession() != null
                                && skill.getJobProfession().getId().equals(professionId));

                if (!allSkillsValid) {
                    throw new RuntimeException("Some skills do not belong to the selected JobProfession");
                }
            }

            jobInDB.setSkills(dbSkills);
        }

        // Check company
        if (job.getCompany() != null) {
            Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
            companyOptional.ifPresent(jobInDB::setCompany);
        }

        // Update correct info
        jobInDB.setName(job.getName());
        jobInDB.setMinSalary(job.getMinSalary());
        jobInDB.setMaxSalary(job.getMaxSalary());
        jobInDB.setSalaryType(job.getSalaryType());
        jobInDB.setQuantity(job.getQuantity());
        jobInDB.setLocation(job.getLocation());
        jobInDB.setLevel(job.getLevel());
        jobInDB.setStartDate(job.getStartDate());
        jobInDB.setEndDate(job.getEndDate());
        jobInDB.setDescription(job.getDescription());
        jobInDB.setRequest(job.getRequest());
        jobInDB.setInterest(job.getInterest());
        jobInDB.setWorklocation(job.getWorklocation());
        jobInDB.setWorktime(job.getWorktime());

        // Validate salary
        if (jobInDB.getSalaryType() == SalaryTypeEnum.SPECIFIC) {
            if (jobInDB.getMinSalary() == null || jobInDB.getMaxSalary() == null) {
                throw new RuntimeException("Min and Max salary are required for SPECIFIC salary type");
            }
            if (jobInDB.getMinSalary() > jobInDB.getMaxSalary()) {
                throw new RuntimeException("Min salary cannot be greater than Max salary");
            }
        } else if (jobInDB.getSalaryType() == SalaryTypeEnum.NEGOTIABLE) {
            jobInDB.setMinSalary(null);
            jobInDB.setMaxSalary(null);
        }

        // Update job
        Job currentJob = jobRepository.save(jobInDB);

        return convertToResUpdateJobDTO(currentJob);
    }

    private ResCreateJobDTO convertToResCreateJobDTO(Job job) {
        ResCreateJobDTO res = new ResCreateJobDTO();
        res.setId(job.getId());
        res.setName(job.getName());
        res.setLocation(job.getLocation());
        res.setMinSalary(job.getMinSalary());
        res.setMaxSalary(job.getMaxSalary());
        res.setSalaryType(job.getSalaryType());
        res.setQuantity(job.getQuantity());
        res.setLevel(job.getLevel());
        res.setStartDate(job.getStartDate());
        res.setEndDate(job.getEndDate());
        res.setCreatedAt(job.getCreatedAt());
        res.setCreatedBy(job.getCreatedBy());
        res.setStatus(job.getStatus());
        res.setLogo(job.getCompany().getLogo());
        res.setCompanyName(job.getCompany().getName());
        if (job.getSkills() != null) {
            List<String> skills = job.getSkills()
                    .stream().map(Skill::getName)
                    .collect(Collectors.toList());
            res.setSkills(skills);
        }

        if (job.getJobProfession() != null) {
            res.setJobProfessionName(job.getJobProfession().getName());
        }

        // THÊM THÔNG TIN GÓI DỊCH VỤ
        if (job.getUserPackage() != null && job.getUserPackage().getServicePackage() != null) {
            PackageType packageType = job.getUserPackage().getServicePackage().getPackageType();
            res.setPackageType(packageType);

            // Kiểm tra xem gói còn hạn không
            boolean isActive = job.getUserPackage().getEndDate().isAfter(Instant.now());

            if (isActive) {
                switch (packageType) {
                    case FEATURED_JOB:
                        res.setFeatured(true);
                        res.setHasBoldTitle(true);
                        break;
                    case PRIORITY_BOLD_TITLE:
                        res.setFeatured(false);
                        res.setHasBoldTitle(true);
                        break;
                    case PRIORITY_DISPLAY:
                        res.setFeatured(false);
                        res.setHasBoldTitle(false);
                        break;
                }
            }
        }

        return res;
    }

    private ResUpdateJobDTO convertToResUpdateJobDTO(Job job) {
        ResUpdateJobDTO rs = new ResUpdateJobDTO();
        rs.setId(job.getId());
        rs.setName(job.getName());
        rs.setLocation(job.getLocation());
        rs.setMinSalary(job.getMinSalary());
        rs.setMaxSalary(job.getMaxSalary());
        rs.setSalaryType(job.getSalaryType());
        rs.setQuantity(job.getQuantity());
        rs.setLevel(job.getLevel());
        rs.setStartDate(job.getStartDate());
        rs.setEndDate(job.getEndDate());
        rs.setStatus(job.getStatus());
        rs.setUpdatedAt(job.getUpdatedAt());
        rs.setUpdatedBy(job.getUpdatedBy());
        rs.setLogo(job.getCompany().getLogo());
        rs.setCompanyName(job.getCompany().getName());
        if (job.getJobProfession() != null) {
            rs.setJobProfessionName(job.getJobProfession().getName());
        }

        if (job.getSkills() != null) {
            List<String> skills = job.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());
            rs.setSkills(skills);
        }

        return rs;
    }

    public Optional<Job> getJobById(long id) {
        return jobRepository.findById(id);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public ResultPaginationDTO getAllJob(Specification<Job> spec, Pageable pageable) {
        // Sử dụng query mới với package priority
//        Page<Job> pageJob = jobRepository.findAllWithPackagePriority(JobStatus.APPROVED, pageable);
        Page<Job> pageJob = jobRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setTotal(pageJob.getTotalElements());
        mt.setPages(pageJob.getTotalPages());

        rs.setMeta(mt);

        List<ResCreateJobDTO> listJob = pageJob.getContent()
                .stream().map(this::convertToResCreateJobDTO)
                .collect(Collectors.toList());

        rs.setResult(listJob);

        return rs;
    }
    public ResultPaginationDTO getAllJobByRole(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = jobRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setTotal(pageJob.getTotalElements());
        mt.setPages(pageJob.getTotalPages());

        rs.setMeta(mt);

        List<ResCreateJobDTO> listJob = pageJob.getContent()
                .stream().map(this::convertToResCreateJobDTO)
                .collect(Collectors.toList());

        rs.setResult(listJob);

        return rs;
    }

    public void approveJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.APPROVED);
        jobRepository.save(job);
        notificationService.notifyJobApproval(job);
    }

    public void rejectJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.REJECTED);
        jobRepository.save(job);
        notificationService.notifyJobReject(job);
    }

    public Page<ResCreateJobDTO> getAllJobByCompanyAndStatus(long companyId,  Pageable pageable) {
        companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        Page<Job> jobPage = jobRepository.findByCompanyIdAndStatus(companyId, JobStatus.APPROVED, pageable);
        return jobPage.map(job -> convertToResCreateJobDTO(job));


    }
    public long countApprovedJobsByCompany(Long companyId) {
        return jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.APPROVED);
    }
    public Page<Job> getByJobProfessionAndStatus(Long jobProfessionId,Pageable pageable) {
        jobProfessionRepository.findById(jobProfessionId).orElseThrow(() -> new RuntimeException("Job Profession not found"));
        Page<Job> jobPage = jobRepository.findByJobProfessionIdAndStatus(jobProfessionId, JobStatus.APPROVED, pageable);
        return jobPage;
    }
    }


