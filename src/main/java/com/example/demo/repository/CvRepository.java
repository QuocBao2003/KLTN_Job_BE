package com.example.demo.repository;

import com.example.demo.domain.Cv;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long>, JpaSpecificationExecutor<Cv> {

    /**
     * Tìm tất cả CV của user hiện tại theo email
     */
    List<Cv> findByCreatedBy(String createdBy);

    /**
     * Tìm tất cả CV của user theo user entity
     */
    List<Cv> findByUser(User user);

    /**
     * Tìm CV theo template
     */
    List<Cv> findByCvTemplate(String cvTemplate);

    /**
     * Đếm số lượng CV của user
     */
    long countByUser(User user);
}