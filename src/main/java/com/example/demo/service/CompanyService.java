package com.example.demo.service;

import com.example.demo.domain.Company;
import com.example.demo.domain.User;
import com.example.demo.dto.response.ResultPaginationDTO;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {

        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

   public Company saveCompany( Company company) {
        Optional<String> currentUserLogin= SecurityUtil.getCurrentUserLogin();
        if(currentUserLogin.isPresent()){
            User hrUser = userRepository.findByEmail(currentUserLogin.get()).orElseThrow(
                    ()-> new RuntimeException("User not found")
            );
            if(!hrUser.getRole().getName().equalsIgnoreCase("HR")){
                throw new RuntimeException("User is not HR");
            }
            company.setHr(hrUser);
            Company savedCompany = companyRepository.save(company);

            // Cập nhật lại user để thuộc về company này
            hrUser.setCompany(savedCompany);
            userRepository.save(hrUser);

            return savedCompany;
        }
        return companyRepository.save(company);
   }

    public ResultPaginationDTO findAll(Specification<Company> spec, Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(companies.getNumber() + 1);
        mt.setPageSize(companies.getSize());
        mt.setPages(companies.getTotalPages());
        mt.setTotal(companies.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(companies.getContent());
        return rs;
    }

   public Company updateCompany(Company company) {
        String emailCurrent = SecurityUtil.getCurrentUserLogin().get();
        User hrUser = userRepository.findByEmail(emailCurrent).orElseThrow(()-> new RuntimeException("User not found"));


       Optional<Company> companyOptional = companyRepository.findById(company.getId());
       if(companyOptional.isPresent()) {
           Company currentCompany = companyOptional.get();
           boolean isAdmin = hrUser.getRole().getName().equalsIgnoreCase("ADMIN");
           boolean isHrCpmpany=currentCompany.getHr().getId().equals(hrUser.getId());

           if(!isAdmin && !isHrCpmpany){
               throw new RuntimeException("You don't have permission to update this company");
           }
           currentCompany.setName(company.getName());
           currentCompany.setLogo(company.getLogo());
           currentCompany.setDescription(company.getDescription());
           currentCompany.setAddress(company.getAddress());
           return this.companyRepository.save(currentCompany);
       }
           return null;

   }

    public void deleteCompany(Long id) {
        User currentUser = userRepository.findByEmail(
                SecurityUtil.getCurrentUserLogin()
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseThrow(() -> new RuntimeException("User not found"));

        // Chỉ Admin mới xóa được company
        if(!currentUser.getRole().getName().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("Only admin can delete company");
        }

        Optional<Company> companyOptional = companyRepository.findById(id);
        if(companyOptional.isPresent()) {
            Company com = companyOptional.get();

            // Fetch all user belong to this company
            List<User> userList = this.userRepository.findByCompany(com);

            // Có thể set company = null thay vì xóa user
            // userList.forEach(user -> user.setCompany(null));
            // this.userRepository.saveAll(userList);

            this.userRepository.deleteAll(userList);
        }

        this.companyRepository.deleteById(id);
    }

    public Company findById(Long id) {
        return companyRepository.findById(id).orElseThrow(()-> new RuntimeException("Company not found"));
    }
}
