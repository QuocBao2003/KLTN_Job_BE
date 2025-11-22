package com.example.demo.controller;



import com.example.demo.dto.response.file.ResUploadFileDTO;
import com.example.demo.service.FileService;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> uploadFile(@RequestParam(name = "file", required = false) MultipartFile file,
                                                       @RequestParam("folder") String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedFileTypes = Arrays.asList("png", "jpg", "jpeg", "doc", "pdf", "docx");
        boolean isValid = allowedFileTypes.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));
        if (!isValid) {
            throw new StorageException("Invalid file extension. Only allow " + allowedFileTypes);
        }

        // Upload file lên Cloudinary
        String uploadedUrl = fileService.uploadFile(file, folder);

        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO(uploadedUrl, Instant.now());
        return ResponseEntity.ok(resUploadFileDTO);
    }


}
