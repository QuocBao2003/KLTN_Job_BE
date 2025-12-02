package com.example.demo.dto.response.job;

import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.Enum.LevelEnum;
import com.example.demo.util.Enum.PackageType;
import com.example.demo.util.Enum.SalaryTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResCreateJobDTO {
    private long id;
    private String name;

    private String location;

    private Double minSalary;
    private Double maxSalary;
    private SalaryTypeEnum salaryType;

    private int quantity;

    private String companyName;

    private LevelEnum level;
    private String logo;
    private Instant startDate;
    private Instant endDate;
    private JobStatus status;
    private PackageType packageType; // Loại gói dịch vụ
    @JsonProperty("isFeatured")
    private boolean isFeatured; // Có phải job hấp dẫn không
    @JsonProperty("hasBoldTitle")
    private boolean hasBoldTitle; // Có tiêu đề nổi bật không
    private List<String> skills;
    private String jobProfessionName;
    private Instant createdAt;
    private String createdBy;
}
