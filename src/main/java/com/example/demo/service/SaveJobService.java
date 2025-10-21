package com.example.demo.service;

import com.example.demo.domain.Company;
import com.example.demo.domain.Job;
import com.example.demo.domain.Savejob;
import com.example.demo.domain.User;
import com.example.demo.dto.response.ResSaveJobDTO;
import com.example.demo.repository.SavejobRepository;
import com.example.demo.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaveJobService {
    private final SavejobRepository savejobRepository;
    private final UserService userService;
    private final JobService jobService;
    public SaveJobService(SavejobRepository savejobRepository, UserService userService, JobService jobService) {
        this.savejobRepository = savejobRepository;
        this.userService = userService;
        this.jobService = jobService;
    }

    public Savejob save(Savejob savejob) {
        return this.savejobRepository.save(savejob);
    }
    public ResSaveJobDTO saveJobByUser( Long jobId) throws Exception {
        Optional<String> userName= SecurityUtil.getCurrentUserLogin();

        User user = userService.getUserByEmail(userName.get());

        Job job = jobService.getJobById(jobId).orElse(null);
        if (job == null) {
            throw new Exception("Job không tồn tại");
        }
        if(savejobRepository.existsByUserAndJob(user,job)){
            throw new Exception("Công việc này đã lưu trước đó");
        }
        Savejob savejob = new Savejob();
        savejob.setUser(user);
        savejob.setJob(job);
        savejob.setSaveTime(LocalDateTime.now());
        Savejob save= savejobRepository.save(savejob);
        Company company=job.getCompany();
        return new ResSaveJobDTO(
                job.getId(),
                job.getName(),
                company !=null ? company.getName() : null,
                save.getSaveTime()
        );
    }

    public List<ResSaveJobDTO> getAllSavejobByUser(Long userId) {
        User user=userService.getUserById(userId);
        List<Savejob> savedJobs=savejobRepository.findByUser(user);
        return savedJobs.stream().map(save -> {
            Job job = save.getJob();
            Company company = job.getCompany();

            return new ResSaveJobDTO(
                    job.getId(),
                    job.getName(),
                    company != null ? company.getName() : null,
                    save.getSaveTime()
            );
        }).collect(Collectors.toList());
    }

}
