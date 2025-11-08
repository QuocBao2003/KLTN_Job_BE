package com.example.demo.dto.request.cv;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateCvDTO {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    private String address;

    private String objective;

    private String experience;

    private String education;

    private String skills;

    private String photoUrl;

    private String cvTemplate; // "Tiêu chuẩn", "Thanh Lịch", "Hiện đại"
}