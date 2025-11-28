package com.example.demo.controller;


import com.example.demo.domain.ServicePackage;
import com.example.demo.domain.UserPackage;
import com.example.demo.dto.request.servicePackage.*;
import com.example.demo.service.PackageOrderService;
import com.example.demo.service.ServicePackageService;
import com.example.demo.service.UserPackageService;
import com.example.demo.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/packages")
public class PackageController {

    private final ServicePackageService servicePackageService;
    private final UserPackageService userPackageService;
    private final PackageOrderService packageOrderService;

    public PackageController(ServicePackageService servicePackage, UserPackageService userPackage, PackageOrderService packageOrderService) {
        this.servicePackageService = servicePackage;
        this.userPackageService = userPackage;
        this.packageOrderService = packageOrderService;
    }


    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/service-packages/all")
    public ResponseEntity<List<ResServicePackageDTO>> getAllServicePackages() {
        return ResponseEntity.ok(servicePackageService.getAllPackages());
    }
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/service-packages/{id}")
    @ApiMessage("Get service package by id")
    public ResponseEntity<ResServicePackageDTO> getServicePackageById(@PathVariable Long id) {
        ServicePackage pkg = servicePackageService.getPackageById(id)
                .orElseThrow(() -> new RuntimeException("Service package not found"));

        ResServicePackageDTO dto = new ResServicePackageDTO();
        dto.setId(pkg.getId());
        dto.setName(pkg.getName());
        dto.setDescription(pkg.getDescription());
        dto.setPrice(pkg.getPrice());
        dto.setPackageType(pkg.getPackageType());
        dto.setJobLimit(pkg.getJobLimit());
        dto.setDurationDays(pkg.getDurationDays());
        dto.setActive(pkg.isActive());

        return ResponseEntity.ok(dto);
    }
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/service-packages")
    @ApiMessage("Create service package")
    public ResponseEntity<ResServicePackageDTO> createServicePackage(@Valid @RequestBody ServicePackage servicePackage) {
        return ResponseEntity.ok(servicePackageService.createPackage(servicePackage));
    }
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/service-packages/{id}")
    @ApiMessage("Update service package")
    public ResponseEntity<ResServicePackageDTO> updateServicePackage(
            @PathVariable Long id,
            @Valid @RequestBody ServicePackage servicePackage) {
        return ResponseEntity.ok(servicePackageService.updatePackage(id, servicePackage));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/service-packages/{id}")
    @ApiMessage("Delete service package")
    public ResponseEntity<Void> deleteServicePackage(@PathVariable Long id) {
        servicePackageService.deletePackage(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/service-packages/{id}/toggle-status")
    @ApiMessage("Toggle service package status")
    public ResponseEntity<Void> togglePackageStatus(@PathVariable Long id) {
        servicePackageService.togglePackageStatus(id);
        return ResponseEntity.ok().build();
    }

    // ============ PUBLIC ENDPOINTS ============
    @GetMapping("/service-packages")
    @ApiMessage("Get all service packages")
    public ResponseEntity<List<ResServicePackageDTO>> getActiveServicePackages() {
        return ResponseEntity.ok(servicePackageService.getAllActivePackages());
    }

    @GetMapping("/my-packages")
    @ApiMessage("Get my packages")
    public ResponseEntity<List<ResUserPackageDTO>> getMyPackages() {
        return ResponseEntity.ok(userPackageService.getMyPackages());
    }

    @GetMapping("/my-packages/active")
    @ApiMessage("Get my active packages with remaining jobs")
    public ResponseEntity<List<ResUserPackageDTO>> getActivePackagesWithRemainingJobs() {
        return ResponseEntity.ok(userPackageService.getActivePackagesWithRemainingJobs());
    }

    @PostMapping("/orders")
    @ApiMessage("Create order")
    public ResponseEntity<ResMoMoPaymentDTO> createOrder(@Valid @RequestBody ReqCreateOrderDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(packageOrderService.createOrder(request));
    }

    @GetMapping("/orders")
    @ApiMessage("Get my orders")
    public ResponseEntity<List<ResPackageOrderDTO>> getMyOrders() {
        return ResponseEntity.ok(packageOrderService.getMyOrders());
    }

    @PostMapping("/payment/callback")
    @ApiMessage("MoMo payment callback")
    public ResponseEntity<Void> handlePaymentCallback(@RequestBody Map<String, String> params) {
        packageOrderService.handlePaymentCallback(params);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment/return")
    @ApiMessage("MoMo payment return")
    public ResponseEntity<String> handlePaymentReturn(@RequestParam Map<String, String> params) {
        // Redirect to frontend with payment result
        String resultCode = params.get("resultCode");
        String orderCode = params.get("orderId");

        if ("0".equals(resultCode)) {
            return ResponseEntity.ok("Payment successful for order: " + orderCode);
        } else {
            return ResponseEntity.ok("Payment failed for order: " + orderCode);
        }
    }

}
