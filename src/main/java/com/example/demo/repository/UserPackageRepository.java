package com.example.demo.repository;


import com.example.demo.domain.UserPackage;
import com.example.demo.util.Enum.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage,Long> {
    List<UserPackage> findByUserIdAndStatus(Long userId, PackageStatus status);
    List<UserPackage> findByUserId(Long userId);
    List<UserPackage> findByServicePackageIdAndStatus(Long servicePackageId, PackageStatus status);
    // Tìm các gói còn hạn và còn job để đăng
    @Query("SELECT up FROM UserPackage as up Where up.user.id= :userId " +
            "AND up.status ='ACTIVE' " +
            "AND up.endDate > :now " +
            "AND up.remainingJobCount >0" +
            "ORDER BY up.servicePackage.packageType DESC"
    )
     List<UserPackage> findActivePackagesWithRemainingJobs(@Param("userId") Long userId, @Param("now") Instant now);
}
