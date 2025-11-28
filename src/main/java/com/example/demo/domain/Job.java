package com.example.demo.domain;



import com.example.demo.util.Enum.JobStatus;
import com.example.demo.util.Enum.LevelEnum;
import com.example.demo.util.Enum.SalaryTypeEnum;
import com.example.demo.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private long id;
    @NotBlank(message = "name không được để trống")
    private String name;
    @NotBlank(message = "location không được để trống")
    private String location;
    private Double minSalary;
    private Double maxSalary;

    @Enumerated(EnumType.STRING)
    private SalaryTypeEnum salaryType = SalaryTypeEnum.SPECIFIC;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private LevelEnum level;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String request;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String interest;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String worklocation;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String worktime;
    @Enumerated(EnumType.STRING)
    private JobStatus status=JobStatus.PENDING;


    @ManyToOne
    @JoinColumn(name = "user_package_id")
    private UserPackage userPackage;
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"jobs"})
    @JoinTable(name = "job_skill",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;

    @OneToMany(mappedBy = "job",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Resume> resumes;

    @OneToMany(mappedBy = "job",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Savejob> savejobs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profession_id")
    @JsonIgnoreProperties({"skills", "jobs", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private JobProfession jobProfession;
    @PrePersist
    public void handleBeforeCreateAt(){
        this.createdAt=Instant.now();
        this.createdBy= SecurityUtil.getCurrentUserLogin().isPresent()?
                SecurityUtil.getCurrentUserLogin().get() : "";
    }
    @PreUpdate
    public void handelBeforeUpdateAt(){
        this.updatedAt=Instant.now();
        this.updatedBy=SecurityUtil.getCurrentUserLogin().isPresent()?
                SecurityUtil.getCurrentUserLogin().get() : "";
    }
}