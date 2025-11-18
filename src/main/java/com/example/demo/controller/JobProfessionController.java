package com.example.demo.controller;

import com.example.demo.domain.JobProfession;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.service.JobProfessionService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/job_professions")
public class JobProfessionController {
    private final JobProfessionService service;

    public JobProfessionController(JobProfessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JobProfession> create(@RequestBody JobProfession profession){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(profession));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobProfession> update(@PathVariable Long id, @RequestBody JobProfession profession){
        return ResponseEntity.ok(service.update(id, profession));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobProfession> getById(@PathVariable Long id){
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<JobProfession> spec, Pageable pageable){
        return ResponseEntity.ok(service.getAll(spec,pageable));
    }

}
