package com.example.demo.controller;


import com.example.demo.dto.response.ResSaveJobDTO;
import com.example.demo.service.SaveJobService;
import com.example.demo.util.annotation.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/save-jobs")
public class SaveJobController {
    private final SaveJobService saveJobService;

    public SaveJobController(SaveJobService saveJobService) {
        this.saveJobService = saveJobService;
    }


    @GetMapping("/{userId}")
    @ApiMessage("Get all saved jobs of user")
    public ResponseEntity<List<ResSaveJobDTO>> getSavedJobs(@PathVariable Long userId) throws Exception {
        List<ResSaveJobDTO> jobs = saveJobService.getAllSavejobByUser(userId);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{jobId}")
    @ApiMessage("Lưu job thành công")

    public ResponseEntity<ResSaveJobDTO> saveJob( @PathVariable Long jobId) throws Exception {
        ResSaveJobDTO saveJobDTO= saveJobService.saveJobByUser(jobId);
        return ResponseEntity.ok(saveJobDTO);
    }
}


