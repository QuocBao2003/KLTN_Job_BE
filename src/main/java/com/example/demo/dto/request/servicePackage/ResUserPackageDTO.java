package com.example.demo.dto.request.servicePackage;


import com.example.demo.util.Enum.PackageStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResUserPackageDTO {
    private Long id;
    private ResServicePackageDTO servicePackage;
    private Instant startDate;
    private Instant endDate;
    private int usedJobCount;
    private int remainingJobCount;
    private PackageStatus status;
    private boolean isExpired;
    private long daysRemaining;

}
