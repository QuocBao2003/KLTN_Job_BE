package com.example.demo.repository;

import com.example.demo.domain.Company;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
     Optional<User> findByEmail(String email);
    boolean existsUserByEmail(String email);
    User findByRefreshTokenAndEmail(String refreshToken, String email);
    List<User> findByCompany(Company company);



    // Tìm Hr của công ty
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.role.name = :roleName")
    Optional<User> findByCompanyIdAndRoleName(Long companyId, String roleName);

    boolean existsByEmail(String email);
}
