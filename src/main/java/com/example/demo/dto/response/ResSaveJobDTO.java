package com.example.demo.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;


import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
public class ResSaveJobDTO {

    private Long id;
    private String name;
    private String companyName;
    private String  location;
    private String logo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime saveTime;
}
