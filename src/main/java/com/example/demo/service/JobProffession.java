package com.example.demo.service;

import com.example.demo.domain.JobProfession;
import com.example.demo.repository.JobProfessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class JobProffession {

    private final JobProfessionRepository jobProfessionRepository;

    public JobProffession(JobProfessionRepository jobProfessionRepository) {
        this.jobProfessionRepository = jobProfessionRepository;
    }


    public List<JobProfession> getAllJobProfession() {
        return jobProfessionRepository.findAll();
    }

}
