package com.example.demo.service;

import com.example.demo.domain.ServicePackage;
import com.example.demo.domain.User;
import com.example.demo.domain.UserPackage;
import com.example.demo.dto.request.servicePackage.ResServicePackageDTO;
import com.example.demo.dto.request.servicePackage.ResUserPackageDTO;
import com.example.demo.repository.ServicePackageRepository;
import com.example.demo.repository.UserPackageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.PackageStatus;
import com.example.demo.util.SecurityUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPackageService {
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;


    public UserPackageService(UserPackageRepository userPackageRepository, UserRepository userRepository, ServicePackageRepository servicePackageRepository) {
        this.userPackageRepository = userPackageRepository;
        this.userRepository = userRepository;
        this.servicePackageRepository = servicePackageRepository;
    }

    public List<ResUserPackageDTO> getMyPackages() {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User email not found"));
        List<UserPackage> userPackages = userPackageRepository.findByUserId(user.getId());
        return userPackages.stream()
                .map(userPackage -> convertToDTO(userPackage))
                .collect(Collectors.toList());

    }
    public List<ResUserPackageDTO> getActivePackagesWithRemainingJobs() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserPackage> packages = userPackageRepository
                .findActivePackagesWithRemainingJobs(user.getId(), Instant.now());

        return packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public UserPackage createUserPackage(User user, ServicePackage servicePackage) {
        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setServicePackage(servicePackage);
        userPackage.setStartDate(Instant.now());
        userPackage.setEndDate(Instant.now().plus(servicePackage.getDurationDays(), ChronoUnit.DAYS));
        userPackage.setRemainingJobCount(servicePackage.getJobLimit());
        userPackage.setStatus(PackageStatus.ACTIVE);

        return userPackageRepository.save(userPackage);
    }

    public UserPackage renewUserPackage(Long userPackageId) {
        UserPackage userPackage = userPackageRepository.findById(userPackageId)
                .orElseThrow(() -> new RuntimeException("User package not found"));

        // Gia hạn: tăng endDate lên thêm số ngày của gói
        Instant newEndDate = userPackage.getEndDate().plus(
                userPackage.getServicePackage().getDurationDays(),
                ChronoUnit.DAYS
        );
        userPackage.setEndDate(newEndDate);

        // Reset số lượng job
        userPackage.setRemainingJobCount(
                userPackage.getRemainingJobCount() +
                        userPackage.getServicePackage().getJobLimit()
        );
        userPackage.setStatus(PackageStatus.ACTIVE);

        return userPackageRepository.save(userPackage);
    }

    public void decrementJobCount(Long userPackageId) {
        UserPackage userPackage = userPackageRepository.findById(userPackageId)
                .orElseThrow(() -> new RuntimeException("User package not found"));

        if (userPackage.getRemainingJobCount() <= 0) {
            throw new RuntimeException("No remaining jobs in this package");
        }

        userPackage.setUsedJobCount(userPackage.getUsedJobCount() + 1);
        userPackage.setRemainingJobCount(userPackage.getRemainingJobCount() - 1);
        userPackageRepository.save(userPackage);
    }

    // Scheduled job để check và update status của các package hết hạn
    @Scheduled(cron = "0 0 0 * * ?") // Chạy vào 00:00 mỗi ngày
    public void updateExpiredPackages() {
        List<UserPackage> activePackages = userPackageRepository
                .findByUserIdAndStatus(null, PackageStatus.ACTIVE);

        Instant now = Instant.now();
        for (UserPackage up : activePackages) {
            if (up.getEndDate().isBefore(now)) {
                up.setStatus(PackageStatus.EXPIRED);
                userPackageRepository.save(up);
            }
        }
    }

    private ResUserPackageDTO convertToDTO(UserPackage userPackage) {
        ResUserPackageDTO dto = new ResUserPackageDTO();
        dto.setId(userPackage.getId());

        // Convert service package
        ResServicePackageDTO packageDTO = new ResServicePackageDTO();
        ServicePackage sp = userPackage.getServicePackage();
        packageDTO.setId(sp.getId());
        packageDTO.setName(sp.getName());
        packageDTO.setDescription(sp.getDescription());
        packageDTO.setPrice(sp.getPrice());
        packageDTO.setPackageType(sp.getPackageType());
        packageDTO.setJobLimit(sp.getJobLimit());
        packageDTO.setDurationDays(sp.getDurationDays());

        dto.setServicePackage(packageDTO);
        dto.setStartDate(userPackage.getStartDate());
        dto.setEndDate(userPackage.getEndDate());
        dto.setUsedJobCount(userPackage.getUsedJobCount());
        dto.setRemainingJobCount(userPackage.getRemainingJobCount());
        dto.setStatus(userPackage.getStatus());

        // Calculate if expired and days remaining
        Instant now = Instant.now();
        dto.setExpired(userPackage.getEndDate().isBefore(now));
        long daysRemaining = ChronoUnit.DAYS.between(now, userPackage.getEndDate());
        dto.setDaysRemaining(Math.max(0, daysRemaining));

        return dto;
    }
}
