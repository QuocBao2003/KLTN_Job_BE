package com.example.demo.repository;

import com.example.demo.domain.Job;
import com.example.demo.domain.Savejob;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
@Repository
public interface SavejobRepository extends JpaRepository<Savejob,Long> {
    boolean existsByUserAndJob(User user, Job job);
    List<Savejob> findByUser(User user);
}
