package com.example.demo.dto.response.chatai;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String type;
    private String message;
    private List<JobSuggestion> jobs;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobSuggestion{
        private long id;
        private String name;
        private String companyName;
        private String location;
        private String logo;
        private double salary;
        private String level;
    }

}
