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
public class ResCreateCvDTO {
    private long id;
    private String fullName;
    private String email;
    private String cvTemplate;
    private Instant createdAt;
    private String createdBy;
}