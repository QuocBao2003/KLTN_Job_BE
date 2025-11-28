package com.example.demo.service;


import com.example.demo.domain.ServicePackage;
import com.example.demo.domain.UserPackage;
import com.example.demo.dto.request.servicePackage.ResServicePackageDTO;
import com.example.demo.repository.ServicePackageRepository;
import com.example.demo.repository.UserPackageRepository;
import com.example.demo.util.Enum.PackageStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final UserPackageRepository userPackageRepository;


    public List<ResServicePackageDTO> getAllActivePackages(){
        List<ServicePackage> servicePackages = servicePackageRepository.findAll();
        return servicePackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<ResServicePackageDTO> getAllPackages() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        return packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public Optional<ServicePackage> getPackageById(Long id) {
        return servicePackageRepository.findById(id);
    }
    public ResServicePackageDTO createPackage(ServicePackage servicePackage) {
        if(servicePackage.getPrice() == null || servicePackage.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }
        if (servicePackage.getJobLimit() <= 0) {
            throw new RuntimeException("Job limit must be greater than 0");
        }

        if (servicePackage.getDurationDays() <= 0) {
            throw new RuntimeException("Duration days must be greater than 0");
        }

        ServicePackage savedPackage = servicePackageRepository.save(servicePackage);
        return convertToDTO(savedPackage);
    }
    public ResServicePackageDTO updatePackage(Long id, ServicePackage servicePackage) {
        ServicePackage existingPackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service package not found"));

        // Validate
        if (servicePackage.getPrice() == null || servicePackage.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        if (servicePackage.getJobLimit() <= 0) {
            throw new RuntimeException("Job limit must be greater than 0");
        }

        if (servicePackage.getDurationDays() <= 0) {
            throw new RuntimeException("Duration days must be greater than 0");
        }

        // Update fields
        existingPackage.setName(servicePackage.getName());
        existingPackage.setDescription(servicePackage.getDescription());
        existingPackage.setPrice(servicePackage.getPrice());
        existingPackage.setPackageType(servicePackage.getPackageType());
        existingPackage.setJobLimit(servicePackage.getJobLimit());
        existingPackage.setDurationDays(servicePackage.getDurationDays());
        existingPackage.setActive(servicePackage.isActive());

        ServicePackage updatedPackage = servicePackageRepository.save(existingPackage);
        return convertToDTO(updatedPackage);
    }
    public void deletePackage(Long id) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service package not found"));

        // Kiểm tra xem có user nào đang sử dụng gói này không
        List<UserPackage> activeUserPackages = userPackageRepository
                .findByServicePackageIdAndStatus(id, PackageStatus.ACTIVE);

        if (!activeUserPackages.isEmpty()) {
            throw new RuntimeException("Cannot delete package. There are " +
                    activeUserPackages.size() + " active subscriptions using this package");
        }

        // Soft delete: set active = false thay vì xóa hẳn
        servicePackage.setActive(false);
        servicePackageRepository.save(servicePackage);
    }
    public void togglePackageStatus(Long id) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service package not found"));

        servicePackage.setActive(!servicePackage.isActive());
        servicePackageRepository.save(servicePackage);
    }

    private ResServicePackageDTO convertToDTO(ServicePackage servicePackage) {
        ResServicePackageDTO dto = new ResServicePackageDTO();
        dto.setId(servicePackage.getId());
        dto.setName(servicePackage.getName());
        dto.setDescription(servicePackage.getDescription());
        dto.setPrice(servicePackage.getPrice());
        dto.setPackageType(servicePackage.getPackageType());
        dto.setJobLimit(servicePackage.getJobLimit());
        dto.setDurationDays(servicePackage.getDurationDays());
        dto.setActive(servicePackage.isActive());
        return dto;
    }
}
