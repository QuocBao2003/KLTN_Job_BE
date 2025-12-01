package com.example.demo.service;

import com.example.demo.domain.Cv;
import com.example.demo.domain.User;
import com.example.demo.dto.request.cv.ReqCreateCvDTO;
import com.example.demo.dto.request.cv.ReqUpdateCvDTO;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.dto.response.cv.ResCreateCvDTO;
import com.example.demo.dto.response.cv.ResFetchCvDTO;
import com.example.demo.dto.response.cv.ResUpdateCvDTO;
import com.example.demo.repository.CvRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.node.FilterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CvService {

    private final CvRepository cvRepository;
    private final UserRepository userRepository;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private FilterParser filterParser;

    @Autowired
    private FilterSpecificationConverter filterSpecificationConverter;

    public CvService(CvRepository cvRepository, UserRepository userRepository) {
        this.cvRepository = cvRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tạo CV mới
     */
    public ResCreateCvDTO createCv(ReqCreateCvDTO reqDto) throws IdInvalidException {
        Cv cv = new Cv();
        cv.setFullName(reqDto.getFullName());
        cv.setEmail(reqDto.getEmail());
        cv.setPhone(reqDto.getPhone());
        cv.setAddress(reqDto.getAddress());
        cv.setObjective(reqDto.getObjective());
        cv.setExperience(reqDto.getExperience());
        cv.setEducation(reqDto.getEducation());
        cv.setSkills(reqDto.getSkills());
        cv.setPhotoUrl(reqDto.getPhotoUrl());
        cv.setCvTemplate(reqDto.getCvTemplate());
        cv.setUrl(reqDto.getUrl());


        // Lấy user hiện tại nếu đã login
        Optional<String> currentUserLogin = SecurityUtil.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            User user = userRepository.findByEmail(currentUserLogin.get()).orElse(null);
            if (user != null) {
                cv.setUser(user);
            }
        }

        Cv savedCv = cvRepository.save(cv);

        // Convert to Response DTO
        ResCreateCvDTO response = new ResCreateCvDTO();
        response.setId(savedCv.getId());
        response.setFullName(savedCv.getFullName());
        response.setEmail(savedCv.getEmail());
        response.setCvTemplate(savedCv.getCvTemplate());
        response.setCreatedAt(savedCv.getCreatedAt());
        response.setCreatedBy(savedCv.getCreatedBy());

        return response;
    }

    /**
     * Cập nhật CV
     */
    public ResUpdateCvDTO updateCv(long id, ReqUpdateCvDTO reqDto) throws IdInvalidException {
        Optional<Cv> cvOptional = cvRepository.findById(id);
        if (cvOptional.isEmpty()) {
            throw new IdInvalidException("CV không tồn tại với id = " + id);
        }

        Cv cv = cvOptional.get();

        // Update fields
        if (reqDto.getFullName() != null) {
            cv.setFullName(reqDto.getFullName());
        }
        if (reqDto.getEmail() != null) {
            cv.setEmail(reqDto.getEmail());
        }
        if (reqDto.getPhone() != null) {
            cv.setPhone(reqDto.getPhone());
        }
        if (reqDto.getAddress() != null) {
            cv.setAddress(reqDto.getAddress());
        }
        if (reqDto.getObjective() != null) {
            cv.setObjective(reqDto.getObjective());
        }
        if (reqDto.getExperience() != null) {
            cv.setExperience(reqDto.getExperience());
        }
        if (reqDto.getEducation() != null) {
            cv.setEducation(reqDto.getEducation());
        }
        if (reqDto.getSkills() != null) {
            cv.setSkills(reqDto.getSkills());
        }
        if (reqDto.getPhotoUrl() != null) {
            cv.setPhotoUrl(reqDto.getPhotoUrl());
        }
        if (reqDto.getCvTemplate() != null) {
            cv.setCvTemplate(reqDto.getCvTemplate());
        }
        if (reqDto.getUrl() != null) {
            cv.setUrl(reqDto.getUrl());
        }

        Cv updatedCv = cvRepository.save(cv);

        // Convert to Response DTO
        ResUpdateCvDTO response = new ResUpdateCvDTO();
        response.setId(updatedCv.getId());
        response.setFullName(updatedCv.getFullName());
        response.setEmail(updatedCv.getEmail());
        response.setCvTemplate(updatedCv.getCvTemplate());
        response.setUpdatedAt(updatedCv.getUpdatedAt());
        response.setUpdatedBy(updatedCv.getUpdatedBy());

        return response;
    }

    /**
     * Lấy CV theo ID
     */
    public Optional<Cv> getCvById(long id) {
        return cvRepository.findById(id);
    }

    /**
     * Lấy CV detail theo ID (Response DTO)
     */
    public ResFetchCvDTO fetchCvById(long id) throws IdInvalidException {
        Optional<Cv> cvOptional = cvRepository.findById(id);
        if (cvOptional.isEmpty()) {
            throw new IdInvalidException("CV không tồn tại với id = " + id);
        }
        return convertToFetchDTO(cvOptional.get());
    }

    /**
     * Xóa CV
     */
    public void deleteCv(long id) throws IdInvalidException {
        Optional<Cv> cvOptional = cvRepository.findById(id);
        if (cvOptional.isEmpty()) {
            throw new IdInvalidException("CV không tồn tại với id = " + id);
        }
        cvRepository.deleteById(id);
    }

    /**
     * Lấy tất cả CV của user hiện tại (với phân trang)
     */
    public ResultPaginationDTO fetchCvByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        // Build filter specification
        FilterNode node = filterParser.parse("createdBy='" + email + "'");
        FilterSpecification<Cv> spec = filterSpecificationConverter.convert(node);
        Page<Cv> pageCv = cvRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageCv.getTotalPages());
        mt.setTotal(pageCv.getTotalElements());

        rs.setMeta(mt);

        // Convert to Response DTO list
        List<ResFetchCvDTO> listCv = pageCv.getContent()
                .stream()
                .map(this::convertToFetchDTO)
                .collect(Collectors.toList());

        rs.setResult(listCv);

        return rs;
    }

    /**
     * Lấy tất cả CV (Admin - với filter và phân trang)
     */
    public ResultPaginationDTO fetchAllCv(Specification<Cv> spec, Pageable pageable) {
        Page<Cv> pageCv = cvRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setTotal(pageCv.getTotalElements());
        mt.setPages(pageCv.getTotalPages());

        rs.setMeta(mt);

        // Convert to Response DTO list
        List<ResFetchCvDTO> listCv = pageCv.getContent()
                .stream()
                .map(this::convertToFetchDTO)
                .collect(Collectors.toList());

        rs.setResult(listCv);

        return rs;
    }

    /**
     * Convert Cv entity to ResFetchCvDTO
     */
    private ResFetchCvDTO convertToFetchDTO(Cv cv) {
        ResFetchCvDTO dto = new ResFetchCvDTO();
        dto.setId(cv.getId());
        dto.setFullName(cv.getFullName());
        dto.setEmail(cv.getEmail());
        dto.setPhone(cv.getPhone());
        dto.setAddress(cv.getAddress());
        dto.setObjective(cv.getObjective());
        dto.setExperience(cv.getExperience());
        dto.setEducation(cv.getEducation());
        dto.setSkills(cv.getSkills());
        dto.setPhotoUrl(cv.getPhotoUrl());
        dto.setCvTemplate(cv.getCvTemplate());
        dto.setCreatedAt(cv.getCreatedAt());
        dto.setUpdatedAt(cv.getUpdatedAt());
        dto.setCreatedBy(cv.getCreatedBy());
        dto.setUpdatedBy(cv.getUpdatedBy());
        dto.setUrl(cv.getUrl());

        // Set user info if exists
        if (cv.getUser() != null) {
            ResFetchCvDTO.UserCv userCv = new ResFetchCvDTO.UserCv(
                    cv.getUser().getId(),
                    cv.getUser().getName(),
                    cv.getUser().getEmail()
            );
            dto.setUser(userCv);
        }

        return dto;
    }

    /**
     * Kiểm tra CV có tồn tại không
     */
    public boolean isCvExists(long id) {
        return cvRepository.existsById(id);
    }

    /**
     * Đếm số lượng CV của user
     */
    public long countCvByUser(User user) {
        return cvRepository.countByUser(user);
    }
}