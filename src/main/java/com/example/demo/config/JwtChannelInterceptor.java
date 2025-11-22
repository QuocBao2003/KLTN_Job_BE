package com.example.demo.config;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;

    public JwtChannelInterceptor(SecurityUtil securityUtil, UserRepository userRepository) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("🔌 WebSocket CONNECT - Token: {}", token != null ? "Có" : "Không có");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                try {
                    // ✅ Giải mã token để lấy email
                    Jwt jwt = securityUtil.checkAccessToken(token);
                    String email = jwt.getSubject();
                    log.info("✅ Token hợp lệ - Email: {}", email);
                    // ✅ Tìm user theo email
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc user không tồn tại"));
                    log.info("✅ Tìm thấy user: ID={}, Email={}", user.getId(), user.getEmail());
                    // ✅ Gán user vào WebSocket session
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                    accessor.getSessionAttributes().put("user", user);
                    accessor.getSessionAttributes().put("email", email);
                    // ✅ Set authentication vào WebSocket session
                    accessor.setUser(authentication);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("✅ WebSocket authentication thành công cho user: {}", email);
                } catch (Exception e) {
                    throw new RuntimeException("Token không hợp lệ: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Thiếu header Authorization khi kết nối WebSocket");
            }
        }

        return message;
    }
}