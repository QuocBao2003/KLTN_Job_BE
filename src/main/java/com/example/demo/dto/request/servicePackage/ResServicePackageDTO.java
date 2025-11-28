package com.example.demo.dto.request.servicePackage;

import com.example.demo.util.Enum.PackageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResServicePackageDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private PackageType packageType;
    private int jobLimit;
    private int durationDays;
    private boolean active;

}
