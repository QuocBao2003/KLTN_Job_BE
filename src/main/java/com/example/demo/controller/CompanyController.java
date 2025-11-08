package com.example.demo.controller;


import com.example.demo.domain.Company;

import com.example.demo.domain.User;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CompanyService;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.StorageException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyService companyService;
    private final UserRepository userRepository;
    public CompanyController(CompanyService companyService, FileService fileService, UserRepository userRepository) {
        this.companyService = companyService;

        this.userRepository = userRepository;
    }

    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody Company company) {
        Company createdCompany = companyService.saveCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }



    @GetMapping("/companies/role")
    public ResponseEntity<ResultPaginationDTO> getAllCompaniesByRole(
            @Filter Specification<Company> spec,
            Pageable pageable
    ) {
        User currentUser = userRepository.findByEmail(
                SecurityUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseThrow(() -> new RuntimeException("User not found"));


        if (!currentUser.getRole().getName().equalsIgnoreCase("SUPER_ADMIN") ) {
            Specification<Company> hrSpec = (root, query, cb) ->
                    cb.equal(root.get("hr").get("id"), currentUser.getId());
            spec = spec != null ? spec.and(hrSpec) : hrSpec;
        }


        ResultPaginationDTO result = companyService.findAll(spec, pageable);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/companies")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = companyService.findAll(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company reqCompany) {
        Company updateCompany = this.companyService.updateCompany(reqCompany);
        return ResponseEntity.status(HttpStatus.OK).body(updateCompany);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(null);

    }
    @GetMapping("/companies/{id}")
    @ApiMessage("get company by id")
    public ResponseEntity<Company> fetchCompanyById(@PathVariable("id") long id){
        Company reqCompany = this.companyService.findById(id);
        return ResponseEntity.ok(reqCompany);
    }
}
