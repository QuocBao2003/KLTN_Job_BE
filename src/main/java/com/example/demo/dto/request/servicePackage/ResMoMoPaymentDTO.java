package com.example.demo.dto.request.servicePackage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMoMoPaymentDTO {
    private String payUrl;
    private String orderCode;
    private String requestId;
    private String message;

}
