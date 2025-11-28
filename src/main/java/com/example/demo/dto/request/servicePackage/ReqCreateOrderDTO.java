package com.example.demo.dto.request.servicePackage;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateOrderDTO {
    @NotNull(message = "Service package ID không được để trống")
    private Long servicePackageId;

    private Long userPackageId; // Null nếu mua mới, có giá trị nếu gia hạn

    @NotNull(message = "Order type không được để trống")
    private String orderType;

}
