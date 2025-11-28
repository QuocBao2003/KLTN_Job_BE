package com.example.demo.config;


import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import com.example.demo.util.Enum.GenderEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseInitiallizer implements CommandLineRunner
{

    private final PermissionRepository permissionRepository;
    private final RoleRepository   roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitiallizer(PermissionRepository permissionRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Start database initiallizer");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();
        if(countPermissions==0){
            ArrayList<Permission> arr = new ArrayList<>();
            arr.add(new Permission("Create a company", "/api/v1/companies", "POST", "COMPANIES"));
            arr.add(new Permission("Update a company", "/api/v1/companies", "PUT", "COMPANIES"));
            arr.add(new Permission("Delete a company", "/api/v1/companies/{id}", "DELETE", "COMPANIES"));
            arr.add(new Permission("Get a company by id", "/api/v1/companies/{id}", "GET", "COMPANIES"));
            arr.add(new Permission("Get companies with pagination", "/api/v1/companies", "GET", "COMPANIES"));

            arr.add(new Permission("Create a job", "/api/v1/jobs", "POST", "JOBS"));
            arr.add(new Permission("Update a job", "/api/v1/jobs", "PUT", "JOBS"));
            arr.add(new Permission("Delete a job", "/api/v1/jobs/{id}", "DELETE", "JOBS"));
            arr.add(new Permission("Get a job by id", "/api/v1/jobs/{id}", "GET", "JOBS"));
            arr.add(new Permission("Get jobs with pagination", "/api/v1/jobs", "GET", "JOBS"));

            arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET", "PERMISSIONS"));

            arr.add(new Permission("Create a resume", "/api/v1/resumes", "POST", "RESUMES"));
            arr.add(new Permission("Update a resume", "/api/v1/resumes", "PUT", "RESUMES"));
            arr.add(new Permission("Delete a resume", "/api/v1/resumes/{id}", "DELETE", "RESUMES"));
            arr.add(new Permission("Get a resume by id", "/api/v1/resumes/{id}", "GET", "RESUMES"));
            arr.add(new Permission("Get resumes with pagination", "/api/v1/resumes", "GET", "RESUMES"));

            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "ROLES"));

            arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET", "USERS"));
            arr.add(new Permission("Update Password", "/api/v1/users/{id}", "PUT", "USERS"));

            arr.add(new Permission("Create a subscriber", "/api/v1/subscribers", "POST", "SUBSCRIBERS"));
            arr.add(new Permission("Update a subscriber", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"));
            arr.add(new Permission("Delete a subscriber", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"));
            arr.add(new Permission("Get a subscriber by id", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"));
            arr.add(new Permission("Get subscribers with pagination", "/api/v1/subscribers", "GET", "SUBSCRIBERS"));

            arr.add(new Permission("Download a file", "/api/v1/files", "POST", "FILES"));
            arr.add(new Permission("Upload a file", "/api/v1/files", "GET", "FILES"));
            arr.add(new Permission("Approve a Job", "/api/v1/jobs/{id}/approve", "PUT", "JOBS"));
            arr.add(new Permission("Reject a Job", "/api/v1/jobs/{id}/reject", "PUT", "JOBS"));

            arr.add(new Permission("Create a skill", "/api/v1/skills", "POST", "SKILLS"));
            arr.add(new Permission("Update a skill", "/api/v1/skills/{id}", "PUT", "SKILLS"));
            arr.add(new Permission("Delete a skill", "/api/v1/skills/{id}", "DELETE", "SKILLS"));
            arr.add(new Permission("Get skill with pagination", "/api/v1/skills", "GET", "SKILLS"));


            arr.add(new Permission("Create a JobProfession","/api/v1/job_professions", "POST", "JOBPROFESSIONS"));
            arr.add(new Permission("Update a JobProfession", "/api/v1/job_professions", "PUT", "JOBPROFESSIONS"));
            arr.add(new Permission("Get a JobProfession","/api/v1/job_professions/{id}","GET", "JOBPROFESSIONS"));
            arr.add(new Permission("Delete a JobProfession","/api/v1/job_professions/{id}","DELETE", "JOBPROFESSIONS"));
            arr.add(new Permission("Get all JobProfessions", "/api/v1/job_professions", "GET", "JOBPROFESSIONS"));



            arr.add(new Permission(
                    "View HR Statistics",
                    "/api/v1/statistics/hr",
                    "GET",
                    "STATISTICS"
            ));
            arr.add(new Permission(
                    "View Admin Statistics",
                    "/api/v1/statistics/admin",
                    "GET",
                    "STATISTICS"
            ));
            arr.add(new Permission("Get all service packages (Admin)", "/api/v1/packages/service-packages/all", "GET", "PACKAGES"));
            arr.add(new Permission("Get service package by id", "/api/v1/packages/service-packages/{id}", "GET", "PACKAGES"));
            arr.add(new Permission("Create service package", "/api/v1/packages/service-packages", "POST", "PACKAGES"));
            arr.add(new Permission("Update service package", "/api/v1/packages/service-packages/{id}", "PUT", "PACKAGES"));
            arr.add(new Permission("Delete service package", "/api/v1/packages/service-packages/{id}", "DELETE", "PACKAGES"));
            arr.add(new Permission("Toggle service package status", "/api/v1/packages/service-packages/{id}/toggle-status", "PUT", "PACKAGES"));

            // Public/HR - Xem và mua gói
            arr.add(new Permission("Get active service packages", "/api/v1/packages/service-packages", "GET", "PACKAGES"));
            arr.add(new Permission("Get my packages", "/api/v1/packages/my-packages", "GET", "PACKAGES"));
            arr.add(new Permission("Get my active packages", "/api/v1/packages/my-packages/active", "GET", "PACKAGES"));
            arr.add(new Permission("Create order", "/api/v1/packages/orders", "POST", "PACKAGES"));
            arr.add(new Permission("Get my orders", "/api/v1/packages/orders", "GET", "PACKAGES"));
            this.permissionRepository.saveAll(arr);
        }
        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);

            this.roleRepository.save(adminRole);
        }

        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("hn");

            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("I'm super admin");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            this.userRepository.save(adminUser);
        }

        if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else
            System.out.println(">>> END INIT DATABASE");
    }
    }

