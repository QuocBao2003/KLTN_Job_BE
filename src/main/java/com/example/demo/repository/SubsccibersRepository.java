package com.example.demo.repository;

import com.example.demo.domain.Subsciber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SubsccibersRepository extends JpaRepository<Subsciber, Long>, JpaSpecificationExecutor<Subsciber> {

    boolean existsByEmail(String email);

    Subsciber findByEmail(String email);
}
