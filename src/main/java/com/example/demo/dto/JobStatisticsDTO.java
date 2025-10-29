package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobStatisticsDTO {
    private String level;
    private String location;
    private String companyName;
    private Long jobCount;
    private Double averageSalary;

    // Constructor cho statistics by level
    public JobStatisticsDTO(String level, Long jobCount, Double averageSalary) {
        this.level = level;
        this.jobCount = jobCount;
        this.averageSalary = averageSalary;
    }

    // Constructor cho statistics by location
    public static JobStatisticsDTO forLocation(String location, Long jobCount, Double averageSalary) {
        JobStatisticsDTO dto = new JobStatisticsDTO();
        dto.setLocation(location);
        dto.setJobCount(jobCount);
        dto.setAverageSalary(averageSalary);
        return dto;
    }

    // Constructor cho statistics by company
    public static JobStatisticsDTO forCompany(String companyName, Long jobCount, Double averageSalary) {
        JobStatisticsDTO dto = new JobStatisticsDTO();
        dto.setCompanyName(companyName);
        dto.setJobCount(jobCount);
        dto.setAverageSalary(averageSalary);
        return dto;
    }
}