package com.example.demo.controller;

import com.example.demo.domain.Job;
import com.example.demo.dto.response.job.ResCreateJobDTO;
import com.example.demo.dto.response.job.ResUpdateJobDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.service.JobService;
import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.Enum.LevelEnum;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.persistence.criteria.JoinType;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a new job")
    public ResponseEntity<ResCreateJobDTO> createJob(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.jobService.createJob(job));

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
    @ApiMessage("GET JOBs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(
            @Filter Specification<Job> spec,
            Pageable pageable,
            @RequestParam(value = "jobProfession", required = false) String[] jobProfession,
            @RequestParam(value = "skills", required = false) String[] skills,
            @RequestParam(value = "level", required = false) String[] level // <--- 2. THÊM THAM SỐ LEVEL
    ){
        // Mặc định chỉ lấy job đã APPROVED
        Specification<Job> pSpec = (root, query, cb) ->
                cb.equal(root.get("status"), JobStatus.APPROVED);

        // --- Logic cũ: Filter JobProfession ---
        if (jobProfession != null && jobProfession.length > 0) {
            Specification<Job> professionSpec = (root, query, cb) -> {
                List<Long> professionIds = Arrays.stream(jobProfession)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                return root.get("jobProfession").get("id").in(professionIds);
            };
            pSpec = pSpec.and(professionSpec);
        }

        // --- Logic cũ: Filter Skills ---
        if (skills != null && skills.length > 0) {
            Specification<Job> skillSpec = (root, query, cb) -> {
                query.distinct(true);
                List<Long> skillIds = Arrays.stream(skills)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                return root.join("skills", JoinType.INNER).get("id").in(skillIds);
            };
            pSpec = pSpec.and(skillSpec);
        }

        // --- 3. LOGIC MỚI: FILTER LEVEL ---
        if (level != null && level.length > 0) {
            Specification<Job> levelSpec = (root, query, cb) -> {
                // Convert mảng String (từ URL) sang List<LevelEnum>
                List<LevelEnum> levelEnums = new ArrayList<>();
                for (String l : level) {
                    try {
                        // Map chuỗi "INTERN" -> Enum INTERN
                        levelEnums.add(LevelEnum.valueOf(l.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Bỏ qua nếu frontend gửi string linh tinh không đúng Enum
                    }
                }
                // Tạo câu lệnh SQL: WHERE level IN ('INTERN', 'JUNIOR', ...)
                return root.get("level").in(levelEnums);
            };
            pSpec = pSpec.and(levelSpec);
        }

        // --- Logic cũ: Kết hợp với spec từ thư viện filter ---
        if (spec != null) {
            pSpec = pSpec.and(spec);
        }

        return ResponseEntity.ok(this.jobService.getAllJob(pSpec, pageable));
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
>>>>>>> Stashed changes

}