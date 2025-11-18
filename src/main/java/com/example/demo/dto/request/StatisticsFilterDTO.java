package com.example.demo.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsFilterDTO {
    private Instant startDate;
    private Instant endDate;
    private String timeUnit = "MONTH"; // "WEEK" hoặc "MONTH"
    private Integer topLimit = 10; // Default top 10 cho admin

    // Validate và set default
    public void validate() {
        if (endDate == null) {
            endDate = Instant.now();
        }

        if (startDate == null) {
            // Default dựa trên timeUnit
            if ("WEEK".equalsIgnoreCase(timeUnit)) {
                // 12 tuần trước (3 tháng)
                startDate = endDate.minus(84, ChronoUnit.DAYS);
            } else {
                // 12 tháng trước
                startDate = endDate.minus(365, ChronoUnit.DAYS);
            }
        }

        if (timeUnit == null || timeUnit.isEmpty()) {
            timeUnit = "MONTH";
        }

        // Validate timeUnit
        if (!timeUnit.equalsIgnoreCase("WEEK") && !timeUnit.equalsIgnoreCase("MONTH")) {
            timeUnit = "MONTH";
        }

        if (topLimit == null || topLimit <= 0) {
            topLimit = 10;
        }
        if (topLimit > 100) {
            topLimit = 100;
        }
    }
}
