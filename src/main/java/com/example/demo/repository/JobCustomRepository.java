package com.example.demo.repository;

import com.example.demo.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface JobCustomRepository {
    Page<Job> findAllWithPackagePriority(Specification<Job> spec, Pageable pageable);
}
