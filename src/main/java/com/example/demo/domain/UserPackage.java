package com.example.demo.domain;

import com.example.demo.util.Enum.PackageStatus;
import com.example.demo.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="user_packages")
public class UserPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "service_package_id")
    private ServicePackage servicePackage;


    private Instant startDate;

    private Instant endDate;


    private int usedJobCount = 0;


    private int remainingJobCount;

    @Enumerated(EnumType.STRING)
    private PackageStatus status = PackageStatus.ACTIVE;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "userPackage", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Job> jobs;

    @OneToMany(mappedBy = "userPackage", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PackageOrder> orders;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
