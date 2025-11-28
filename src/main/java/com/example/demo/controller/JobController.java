package com.example.demo.controller;

import com.example.demo.domain.Company;
import com.example.demo.domain.Job;
import com.example.demo.domain.User;
import com.example.demo.dto.response.job.ResCreateJobDTO;
import com.example.demo.dto.response.job.ResUpdateJobDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JobService;
import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class JobController {
    private final JobService jobService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    public JobController(JobService jobService, UserRepository userRepository, CompanyRepository companyRepository) {
        this.jobService = jobService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a new job")
    public ResponseEntity<ResCreateJobDTO> createJob(
            @Valid @RequestBody Job job,
            @RequestParam Long userPackageId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.jobService.createJob(job, userPackageId));
    }
    @PutMapping("/jobs")
    @ApiMessage("Update job")
    public ResponseEntity<ResUpdateJobDTO> updateJob(@Valid @RequestBody Job job) throws IdInvalidException {
        Optional<Job> currnentJob = this.jobService.getJobById(job.getId());
        if(!currnentJob.isPresent()){
            throw  new IdInvalidException("Job not found");
        }
        return ResponseEntity.ok().body(this.jobService.updateJob(job,currnentJob.get()));
    }
    @DeleteMapping("/jobs/{id}")
    @ApiMessage("Delete job")
    public ResponseEntity<Void> handleDeleteJob(@PathVariable("id") long id){
        this.jobService.deleteJob(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/jobs/{id}")
    @ApiMessage("Get job by id")
    public ResponseEntity<Job> getJobById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> job = this.jobService.getJobById(id);
        if(!job.isPresent()){
            throw  new IdInvalidException("Job not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(job.get());
    }

    @GetMapping("/jobs")
    @ApiMessage("Get all public jobs with package priority")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(
            @Filter Specification<Job> spec,
            Pageable pageable
            ){
        Specification<Job> approved = (root, query, cb) ->
                cb.equal(root.get("status"), JobStatus.APPROVED);

        spec = spec != null ? spec.and(approved) : approved;
        return ResponseEntity.ok(this.jobService.getAllJob(spec,pageable));
    }
    @GetMapping("/jobs/jobProfession/{jobProfessionId}")
    @ApiMessage("Get job by profession")
    public ResponseEntity<Page<Job>> getJobByJobProfesionAndStatus(@PathVariable("jobProfessionId") long jobProfessionId,Pageable pageable){
        return ResponseEntity.ok(jobService.getByJobProfessionAndStatus(jobProfessionId,pageable));
    }
    @GetMapping("/jobs/role")
    @ApiMessage("Job map Role")
    public ResponseEntity<ResultPaginationDTO> getAllJobsMapByRole(
            @Filter Specification<Job> spec,
            Pageable pageable
    ){
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User current = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = current.getRole().getName();

        if (role.equalsIgnoreCase("HR")) {
            List<Long> companyIds = companyRepository.findAllByHrId(current.getId()).stream().map(Company::getId).toList();
            Specification<Job> filterByCompanies = (root, query, cb) ->
                    root.get("company").get("id").in(companyIds);

            spec = spec != null ? spec.and(filterByCompanies) : filterByCompanies;
        }

        return ResponseEntity.ok(jobService.getAllJobByRole(spec, pageable));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/jobs/{id}/approve")
    @ApiMessage("Approve job")
    public ResponseEntity<Void> approveJob(@PathVariable long id) {
        jobService.approveJob(id);
        return ResponseEntity.ok(null);
    }
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/jobs/{id}/reject")
    @ApiMessage("Reject job")
    public ResponseEntity<Void> rejectJob(@PathVariable long id) {
        jobService.rejectJob(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/jobs/company/{companyId}")
    @ApiMessage("Get approved jobs by company")
    public ResponseEntity<ResultPaginationDTO> getApprovedJobsByCompany(
            @PathVariable Long companyId,
            Pageable pageable
    ) {
        Page<ResCreateJobDTO> result = jobService.getAllJobByCompanyAndStatus(companyId, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setTotal(result.getTotalElements());
        mt.setPages(result.getTotalPages());
        rs.setMeta(mt);
        rs.setResult(result.getContent());

        return ResponseEntity.ok(rs);
    }
    @GetMapping("/jobs/company/{companyId}/count")
    @ApiMessage("Count approved jobs by company")
    public ResponseEntity<Map<String, Object>> countApprovedJobsByCompany(
            @PathVariable Long companyId
    ) {
        long count = jobService.countApprovedJobsByCompany(companyId);

        Map<String, Object> response = new HashMap<>();
        response.put("companyId", companyId);
        response.put("approvedJobsCount", count);

        return ResponseEntity.ok(response);
    }
}
