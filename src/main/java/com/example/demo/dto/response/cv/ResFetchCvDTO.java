package com.example.demo.dto.response.cv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResFetchCvDTO {
    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String objective;
    private String experience;
    private String education;
    private String skills;
    private String photoUrl;
    private String cvTemplate;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private UserCv user;
    private String url;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserCv {
        private long id;
        private String name;
        private String email;
    }
}