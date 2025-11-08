package com.example.demo.controller;


import com.example.demo.domain.Company;
import com.example.demo.domain.Job;
import com.example.demo.domain.Resume;
import com.example.demo.domain.User;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.dto.response.resume.ResCreateResumeDTO;
import com.example.demo.dto.response.resume.ResFetchResumeDTO;
import com.example.demo.dto.response.resume.ResUpdateResumeDTO;
import com.example.demo.service.ResumeService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {
    private final ResumeService resumeService;
    private final UserService userService;
    private final FilterSpecificationConverter filterSpecificationConverter;
    private final FilterBuilder filterBuilder;
    public ResumeController(ResumeService resumeService, UserService userService, FilterSpecificationConverter filterSpecificationConverter, FilterBuilder filterBuilder) {
        this.resumeService = resumeService;

        this.userService = userService;
        this.filterSpecificationConverter = filterSpecificationConverter;
        this.filterBuilder = filterBuilder;
    }

    @PostMapping("/resumes")
    @ApiMessage("create resume")
    public ResponseEntity<ResCreateResumeDTO> createResume(@Valid @RequestBody Resume resume) throws IdInvalidException {
        boolean ischeckResume = this.resumeService.checkResumeExistsByUserAndJob(resume);
        if(!ischeckResume) {
            throw  new IdInvalidException("User id/Job không tồn tại");
        }
        return ResponseEntity.ok().body(this.resumeService.createResume(resume));

    }

    @PutMapping("/resumes")
    @ApiMessage("update resume")
    public ResponseEntity<ResUpdateResumeDTO> updateResume( @RequestBody Resume resume) throws IdInvalidException {
        Optional<Resume> reqResumeOptional = this.resumeService.getResumeById(resume.getId());
        if(reqResumeOptional.isEmpty()){
            throw new IdInvalidException("Resume với id"+resume.getId()+"không tồn tại");
        }
        Resume reqResume = reqResumeOptional.get();
        reqResume.setStatus(resume.getStatus());

        return ResponseEntity.ok().body(this.resumeService.update(reqResume));

    }
    @DeleteMapping("/resumes/{id}")
    @ApiMessage("Delete resumes")
    public ResponseEntity<Void> deleteResume(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> resume=this.resumeService.getResumeById(id);
        if(resume.isEmpty()){
            throw new IdInvalidException("Resume với id"+id+"không tồn tại");
        }
        this.resumeService.deleteResumeById(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/resumes/{id}")
    @ApiMessage("get a resume")
    public ResponseEntity<ResFetchResumeDTO> fetchResumeById(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> resume = this.resumeService.getResumeById(id);
        if(resume.isEmpty()){
            throw new IdInvalidException("Resume với id"+id+"không tồn tại");
        }
        return ResponseEntity.ok().body(this.resumeService.getResume(resume.get()));
    }

    @GetMapping("/resumes")
    @ApiMessage("Fetch all resumes with paginate")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<Resume> spec,
            Pageable pageable) {

        User currentUser = userService.handleGetUserByUserName(
                SecurityUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseThrow(() -> new RuntimeException("User not found"));

        String role = currentUser.getRole().getName();

        // Nếu HR => chỉ được xem resume của company mình quản lý
        if (role.equalsIgnoreCase("HR")) {

            Company userCompany = currentUser.getCompany();
            if (userCompany == null) {
                throw new RuntimeException("HR does not belong to any company");
            }

            Specification<Resume> companyFilter = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), userCompany.getId());

            spec = (spec != null) ? spec.and(companyFilter) : companyFilter;
        }




        return ResponseEntity.ok(resumeService.fetchAllResume(spec, pageable));
    }

    @GetMapping("/resumes/by-user")
    @ApiMessage("Get list resumes by user")
    public ResponseEntity<ResultPaginationDTO> fetchResumeByUser(Pageable pageable) {

        return ResponseEntity.ok().body(this.resumeService.fetchResumeByUser(pageable));
    }
}
