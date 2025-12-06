package com.example.demo.service;


import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ForgotPasswordService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public ForgotPasswordService(EmailService emailService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            throw new RuntimeException("email không tồn tại");
        }
        String password = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        emailService.sendEmail(email,"Mật khẩu mới của bạn","Mật khẩu mới" + password);
}
    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
