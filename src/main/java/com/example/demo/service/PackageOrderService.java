package com.example.demo.service;


import com.example.demo.domain.PackageOrder;
import com.example.demo.domain.ServicePackage;
import com.example.demo.domain.User;
import com.example.demo.domain.UserPackage;
import com.example.demo.dto.request.servicePackage.ReqCreateOrderDTO;
import com.example.demo.dto.request.servicePackage.ResMoMoPaymentDTO;
import com.example.demo.dto.request.servicePackage.ResPackageOrderDTO;
import com.example.demo.dto.request.servicePackage.ResServicePackageDTO;
import com.example.demo.repository.PackageOrderRepository;
import com.example.demo.repository.ServicePackageRepository;
import com.example.demo.repository.UserPackageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.OrderType;
import com.example.demo.util.Enum.PaymentMethod;
import com.example.demo.util.Enum.PaymentStatus;
import com.example.demo.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PackageOrderService {
    private final PackageOrderRepository packageOrderRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;
    private final UserPackageService userPackageService;
    private final MoMoPaymentService moMoPaymentService;

    public PackageOrderService(PackageOrderRepository packageOrderRepository, ServicePackageRepository servicePackageRepository, UserPackageRepository userPackageRepository, UserRepository userRepository, UserPackageService userPackageService, MoMoPaymentService moMoPaymentService) {
        this.packageOrderRepository = packageOrderRepository;
        this.servicePackageRepository = servicePackageRepository;
        this.userPackageRepository = userPackageRepository;
        this.userRepository = userRepository;
        this.userPackageService = userPackageService;
        this.moMoPaymentService = moMoPaymentService;
    }
    public ResMoMoPaymentDTO createOrder(ReqCreateOrderDTO request) {
        // Get current user
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is HR
        if (!user.getRole().getName().equals("HR")) {
            throw new RuntimeException("Only HR can purchase packages");
        }

        // Get service package
        ServicePackage servicePackage = servicePackageRepository
                .findByIdAndActiveTrue(request.getServicePackageId())
                .orElseThrow(() -> new RuntimeException("Service package not found"));

        OrderType orderType;
        try {
            orderType = OrderType.valueOf(request.getOrderType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order type: " + request.getOrderType());
        }

        UserPackage userPackage = null;
        if (orderType == OrderType.RENEWAL) {
            if (request.getUserPackageId() == null) {
                throw new RuntimeException("User package ID is required for renewal");
            }
            userPackage = userPackageRepository.findById(request.getUserPackageId())
                    .orElseThrow(() -> new RuntimeException("User package not found"));

            // Validate user package belongs to current user
            if (!userPackage.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("User package does not belong to current user");
            }
        }

        // Create order
        PackageOrder order = new PackageOrder();
        order.setUser(user);
        order.setServicePackage(servicePackage);
        order.setUserPackage(userPackage);
        order.setOrderCode("ORD" + System.currentTimeMillis());
        order.setAmount(servicePackage.getPrice());
        order.setOrderType(OrderType.valueOf(String.valueOf(request.getOrderType())));
        order.setPaymentMethod(PaymentMethod.MOMO);
        order.setPaymentStatus(PaymentStatus.PENDING);

        PackageOrder savedOrder = packageOrderRepository.save(order);

        // Create MoMo payment
        String orderInfo = orderType == OrderType.NEW_PURCHASE
                ? "Mua gói dịch vụ: " + servicePackage.getName()
                : "Gia hạn gói dịch vụ: " + servicePackage.getName();

        ResMoMoPaymentDTO payment = moMoPaymentService.createPayment(
                savedOrder.getOrderCode(),
                savedOrder.getAmount(),
                orderInfo
        );

        // Update order with request ID
        savedOrder.setRequestId(payment.getRequestId());
        packageOrderRepository.save(savedOrder);

        return payment;
    }
    public void handlePaymentCallback(Map<String, String> params) {
       log.info("payment callback received" , params);
        // Verify signature
        if (!moMoPaymentService.verifySignature(params)) {
            log.error("Invalid signature for params: {}", params);
            throw new RuntimeException("Invalid signature");
        }

        String orderCode = params.get("orderId");
        log.info("Processing order: {}", orderCode);
        String resultCode = params.get("resultCode");
        String transId = params.get("transId");

        PackageOrder order = packageOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("0".equals(resultCode)) {
            // Payment success
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setTransactionId(transId);
            packageOrderRepository.save(order);

            // Create or renew user package
            if (order.getOrderType() == OrderType.NEW_PURCHASE) {
                userPackageService.createUserPackage(order.getUser(), order.getServicePackage());
            } else if (order.getOrderType() == OrderType.RENEWAL) {
                if (order.getUserPackage() != null) {
                    userPackageService.renewUserPackage(order.getUserPackage().getId());
                } else {
                    throw new RuntimeException("User package not found for renewal");
                }
            }
        } else {
            // Payment failed
            order.setPaymentStatus(PaymentStatus.FAILED);
            packageOrderRepository.save(order);
        }
    }

    public List<ResPackageOrderDTO> getMyOrders() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PackageOrder> orders = packageOrderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ResPackageOrderDTO convertToDTO(PackageOrder order) {
        ResPackageOrderDTO dto = new ResPackageOrderDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());

        // Convert service package
        ResServicePackageDTO packageDTO = new ResServicePackageDTO();
        ServicePackage sp = order.getServicePackage();
        packageDTO.setId(sp.getId());
        packageDTO.setName(sp.getName());
        packageDTO.setDescription(sp.getDescription());
        packageDTO.setPrice(sp.getPrice());
        packageDTO.setPackageType(sp.getPackageType());
        packageDTO.setJobLimit(sp.getJobLimit());
        packageDTO.setDurationDays(sp.getDurationDays());

        dto.setServicePackage(packageDTO);
        dto.setAmount(order.getAmount());
        dto.setOrderType(order.getOrderType());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTransactionId(order.getTransactionId());
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }
}
