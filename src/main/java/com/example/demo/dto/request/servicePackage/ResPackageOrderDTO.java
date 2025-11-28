package com.example.demo.dto.request.servicePackage;

import com.example.demo.util.Enum.OrderType;
import com.example.demo.util.Enum.PaymentMethod;
import com.example.demo.util.Enum.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResPackageOrderDTO {
    private Long id;
    private String orderCode;
    private ResServicePackageDTO servicePackage;
    private Double amount;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private Instant createdAt;
}
