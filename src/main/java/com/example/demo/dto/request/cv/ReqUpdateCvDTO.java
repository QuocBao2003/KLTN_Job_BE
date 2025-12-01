package com.example.demo.dto.request.cv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateCvDTO {

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
    private String url;
}