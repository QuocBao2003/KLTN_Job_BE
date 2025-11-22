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
    public ResSaveJobDTO saveJobByUser(Long jobId) throws Exception {
        // Lấy email user hiện tại từ SecurityUtil
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new Exception("Bạn chưa đăng nhập"));

        // Tìm user theo email
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin người dùng"));

        // Tìm job theo ID
        Job job = jobService.getJobById(jobId)
                .orElseThrow(() -> new Exception("Job không tồn tại"));

        // Kiểm tra nếu đã lưu job này rồi
        if (savejobRepository.existsByUserAndJob(user, job)) {
            throw new Exception("Công việc này đã được lưu trước đó");
        }

        // Tạo bản ghi SaveJob
        Savejob savejob = new Savejob();
        savejob.setUser(user);
        savejob.setJob(job);
        savejob.setSaveTime(LocalDateTime.now());
        Savejob saved = savejobRepository.save(savejob);

        Company company = job.getCompany();

        // Trả về DTO
        return new ResSaveJobDTO(
                job.getId(),
                job.getName(),
                company != null ? company.getName() : null,
                job.getLocation(),
                saved.getSaveTime()
        );
    }

    public List<ResSaveJobDTO> getAllSavejobByUser() throws Exception {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new Exception("Bạn chưa đăng nhập"));

        // Tìm user theo email
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin người dùng"));

        List<Savejob> savedJobs=savejobRepository.findByUser(user);
        return savedJobs.stream().map(save -> {
            Job job = save.getJob();
            Company company = job.getCompany();

            return new ResSaveJobDTO(
                    job.getId(),
                    job.getName(),
                    company != null ? company.getName() : null,
                    job.getLocation(),
                    save.getSaveTime()
            );
        }).collect(Collectors.toList());
    }

}
