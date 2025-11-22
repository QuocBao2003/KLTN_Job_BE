package com.example.demo.repository;

import com.example.demo.domain.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {
    boolean existsByModuleAndApiPathAndMethod(String module,String apiPath,String method);
    List<Permission> findByIdIn(List<Long> id);
    Page<Permission> findAll(Specification<Permission> spec, Pageable pageable);
}
