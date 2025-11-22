package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

public class JobFilterDTO {

    // DTO cho JobProfession (Cấp 1)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ProfessionResponse {
        private Long id;
        private String name;
        private List<JobResponse> jobs;
    }

    // DTO cho Job (Cấp 2)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class JobResponse {
        private Long id;
        private String name;
        private List<SkillResponse> skills;
    }

    // DTO cho Skill (Cấp 3)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SkillResponse {
        private Long id;
        private String name;
    }
}
