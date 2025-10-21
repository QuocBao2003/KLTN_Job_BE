package com.example.demo.repository;

import com.example.demo.domain.Job;
import com.example.demo.domain.Savejob;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface SavejobRepository extends JpaRepository<Savejob,Long> {
    boolean existsByUserAndJob(User user, Job job);
    List<Savejob> findByUser(User user);
}
