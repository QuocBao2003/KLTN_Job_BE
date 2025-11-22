package com.example.demo.util;


import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.request.ExchangeTokenRequest;
import com.example.demo.dto.response.ResLoginDTO;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.httpclient.OutboundIndentityClient;
import com.example.demo.repository.httpclient.OutboundUserClient;


import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Slf4j
@Service


public class SecurityUtil
{
    private final JwtEncoder jwtEncoder;
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    @Value("${demo.jwt.base64-secret}")
    private String jwtKey;
    @Autowired
    private OutboundIndentityClient  outboundIndentityClient;
    @Value("${demo.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${demo.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;
    @NonFinal
    @Value("${outbound.indentity.client-id}")
    protected String CLIENT_ID;
    @NonFinal
    @Value("${outbound.indentity.client-secret}")
    protected String CLIENT_SECRET ;
    @NonFinal
    @Value("${outbound.indentity.redirect-uri}")
    protected String REDIRECT_URL ;

    @NonFinal
    @Value("${outbound.indentity.grant-type}")
    protected  String GRANT_TYPE ;
   @Autowired
    private OutboundUserClient outboundUserClient;
   @Autowired
   private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;


    }
//    create token
    public String createAccessToken(String email, ResLoginDTO resDTO){
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(resDTO.getUser().getId());
        userToken.setEmail(resDTO.getUser().getEmail());
        userToken.setName(resDTO.getUser().getName());
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

//        hardmode permission
        List<String> listAuthority = new ArrayList<String>();
        if(resDTO.getUser().getRole() !=null){
            listAuthority= resDTO.getUser().getRole().getPermissions().stream().map(Permission::getName).toList();
        }
        Role role = roleRepository.findByName(resDTO.getUser().getRole().getName());
//        @formater:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("role",role.getName())
                .claim("permissions",listAuthority)
                .claim("user",userToken)
                .build();

        JwsHeader jwsHeader= JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
    }
//    Refresh token
    public String  createRefreshToken(String email, ResLoginDTO restLoginDTO){
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(restLoginDTO.getUser().getId());
        userToken.setEmail(restLoginDTO.getUser().getEmail());
        userToken.setName(restLoginDTO.getUser().getName());
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

//        @formater:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now) //thời gian phát hành token
                .expiresAt(validity) // thời gian heets hạn
                .subject(email)// định danh người dùng
                .claim("user",userToken) //lấy thông tin người dùng
                .build();

        JwsHeader jwsHeader= JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
    }
    public Jwt checkAccessToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecrectKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            System.out.println("AccessToken error: " + e.getMessage());
            throw e;
        }
    }
    public Jwt checkRefreshToken(String token){
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecrectKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try{
           return jwtDecoder.decode(token);

        }catch( Exception e ){
            System.out.println("RefreshToken error"+e.getMessage());
            throw e;
        }
    }
    public ResLoginDTO outboundAuthenticate(String code){
        var response = outboundIndentityClient.exchangeToken(ExchangeTokenRequest.builder()
                        .code(code)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URL)
                        .grantType(GRANT_TYPE)
                .build());
        var userInfo = outboundUserClient.getUserInfomation("Bearer " + response.getAccessToken());
        log.info("User info: {}", userInfo);

        var passwordEncoder = new BCryptPasswordEncoder();
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        Role userRole = roleRepository.findByName("USER");

        User user;
        if (userRepository.existsByEmail(userInfo.getEmail())) {
            user = userRepository.findByEmail(userInfo.getEmail()).get();
        } else {
            user = userRepository.save(User.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .password(randomPassword)
                    .role(userRole)
                    .build());
        }
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setEmail(user.getEmail());
        userLogin.setName(user.getName());
        userLogin.setId(user.getId());
        userLogin.setRole(user.getRole());
        ResLoginDTO resDTO = ResLoginDTO.builder()
                .user(userLogin)
                .build();
        String accessToken = createAccessToken(user.getEmail(), resDTO);
        log.info("accessToken: {}", accessToken);
//        String refreshToken = createRefreshToken(user.getEmail(), resDTO);
//        log.info("refreshToken: {}", refreshToken);
        return ResLoginDTO.builder()
                .accessToken(accessToken)
                .user(userLogin)
                .build();
    }

    private SecretKey getSecrectKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtKey);
        // HS512 tương ứng với HmacSHA512 trong JCA
        System.out.println("Key lengthss (bytes1): " + keyBytes);
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }
    public static Optional<String> getCurrentUserLogin(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication){
        if(authentication == null){
            return null;
        }else if(authentication.getPrincipal() instanceof UserDetails springSecurityUser){
            return springSecurityUser.getUsername();
        }else if(authentication.getPrincipal() instanceof Jwt jwt){
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }
    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
//     public static boolean isAuthenticated() {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         return authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals);
//     }

    /**
     * Checks if the current user has any of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has any of the authorities, false otherwise.
     */
//     public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         return (
//             authentication != null && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority))
//         );
//     }

    /**
     * Checks if the current user has none of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has none of the authorities, false otherwise.
     */
//     public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
//         return !hasCurrentUserAnyOfAuthorities(authorities);
//     }

    /**
     * Checks if the current user has a specific authority.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
//     public static boolean hasCurrentUserThisAuthority(String authority) {
//         return hasCurrentUserAnyOfAuthorities(authority);
//     }
//
//     private static Stream<String> getAuthorities(Authentication authentication) {
//         return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
//     }

}
