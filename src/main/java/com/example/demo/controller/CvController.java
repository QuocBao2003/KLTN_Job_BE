// Thêm vào CvController.java

package com.example.demo.controller;

import com.example.demo.domain.Cv;
import com.example.demo.dto.request.cv.ReqCreateCvDTO;
import com.example.demo.dto.request.cv.ReqUpdateCvDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.dto.response.cv.ResCreateCvDTO;
import com.example.demo.dto.response.cv.ResFetchCvDTO;
import com.example.demo.dto.response.cv.ResUpdateCvDTO;
import com.example.demo.service.CvService;
import com.example.demo.service.ExcelService;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CvController {

    private final CvService cvService;
    private final ExcelService excelService;

    public CvController(CvService cvService, ExcelService excelService) {
        this.cvService = cvService;
        this.excelService = excelService;
    }

    /**
     * API upload Excel CV
     * POST /api/v1/cvs/upload-excel
     */
    @PostMapping("/cvs/upload-excel")
    @ApiMessage("Upload Excel CV thành công")
    public ResponseEntity<List<Cv>> uploadExcelCv(@RequestParam("file") MultipartFile file)
            throws IdInvalidException, IOException {
        List<Cv> cvList = excelService.uploadExcelCv(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(cvList);
    }

    /**
     * API tạo CV mới (từ Frontend)
     * POST /api/submit-cv
     */
    @PostMapping("/submit-cv")
    @ApiMessage("Tạo CV thành công")
    public ResponseEntity<ResCreateCvDTO> submitCv(@Valid @RequestBody ReqCreateCvDTO reqDto)
            throws IdInvalidException {
        ResCreateCvDTO cv = cvService.createCv(reqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cv);
    }

    /**
     * API tạo CV mới (chuẩn REST)
     * POST /api/v1/cvs
     */
    @PostMapping("/cvs")
    @ApiMessage("Tạo CV thành công")
    public ResponseEntity<ResCreateCvDTO> createCv(@Valid @RequestBody ReqCreateCvDTO reqDto)
            throws IdInvalidException {
        ResCreateCvDTO cv = cvService.createCv(reqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cv);
    }

    /**
     * API cập nhật CV
     * PUT /api/v1/cvs/{id}
     */
    @PutMapping("/cvs/{id}")
    @ApiMessage("Cập nhật CV thành công")
    public ResponseEntity<ResUpdateCvDTO> updateCv(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqUpdateCvDTO reqDto) throws IdInvalidException {
        ResUpdateCvDTO cv = cvService.updateCv(id, reqDto);
        return ResponseEntity.ok(cv);
    }

    /**
     * API lấy CV theo ID
     * GET /api/v1/cvs/{id}
     */
    @GetMapping("/cvs/{id}")
    @ApiMessage("Lấy thông tin CV thành công")
    public ResponseEntity<ResFetchCvDTO> getCvById(@PathVariable("id") long id)
            throws IdInvalidException {
        ResFetchCvDTO cv = cvService.fetchCvById(id);
        return ResponseEntity.ok(cv);
    }

    /**
     * API xóa CV
     * DELETE /api/v1/cvs/{id}
     */
    @DeleteMapping("/cvs/{id}")
    @ApiMessage("Xóa CV thành công")
    public ResponseEntity<Void> deleteCv(@PathVariable("id") long id)
            throws IdInvalidException {
        cvService.deleteCv(id);
        return ResponseEntity.ok(null);
    }

    /**
     * API lấy danh sách CV của user hiện tại
     * POST /api/v1/cvs/by-user
     */
    @PostMapping("/cvs/by-user")
    @ApiMessage("Lấy danh sách CV của tôi thành công")
    public ResponseEntity<ResultPaginationDTO> fetchCvByUser(Pageable pageable) {
        ResultPaginationDTO cvs = cvService.fetchCvByUser(pageable);
        return ResponseEntity.ok(cvs);
    }

    /**
     * API lấy tất cả CV (Admin - với filter)
     * GET /api/v1/cvs
     */
    @GetMapping("/cvs")
    @ApiMessage("Lấy danh sách CV thành công")
    public ResponseEntity<ResultPaginationDTO> fetchAllCv(
            @Filter Specification<Cv> spec,
            Pageable pageable) {
        ResultPaginationDTO cvs = cvService.fetchAllCv(spec, pageable);
        return ResponseEntity.ok(cvs);
    }
}