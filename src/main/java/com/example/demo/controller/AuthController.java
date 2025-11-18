package com.example.demo.controller;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.request.RequestLoginDTO;
import com.example.demo.dto.response.ResCreateUserDTO;
import com.example.demo.dto.response.ResLoginDTO;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import com.example.demo.util.error.IdInvalidException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final SecurityUtil securityUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;


    @Value("${demo.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(SecurityUtil securityUtil,
                          AuthenticationManagerBuilder authenticationManagerBuilder,
                          UserService userService,
                          PasswordEncoder passwordEncoder, RoleService roleService

    ) {
        this.securityUtil = securityUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;


        this.roleService = roleService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody RequestLoginDTO loginDTO) {
//        Nạp input gồm username,password vào security
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

//        xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

//        set thông tin người dùng đăng nhập vào ccontext (có thể suwr dụng sau này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO restLoginDTO = new ResLoginDTO();
//        lấy API thông tin người nguười login
        Optional<User> currentUserDB = this.userService.handleGetUserByUserName(loginDTO.getUsername());

        ResLoginDTO.UserLogin userLogin;
        if (currentUserDB != null) {
            User user = currentUserDB.get();
             userLogin = new ResLoginDTO.UserLogin(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole()

            );
            restLoginDTO.setUser(userLogin);
        }
        //    create accsessToken
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), restLoginDTO);
        restLoginDTO.setAccessToken(access_token);

//        create refresh token(email,dto)
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), restLoginDTO);
//        update user
        this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

//        set cookies
        ResponseCookie resCookie=ResponseCookie.from("refresh_token",refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,resCookie.toString())
                .body(restLoginDTO);
    }
    @GetMapping("/auth/account")
    @ApiMessage("Fecth account message")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        Optional<User> currentUser = this.userService.handleGetUserByUserName(email);
        ResLoginDTO.UserLogin userLogin= new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        if(currentUser.isPresent()) {
            userLogin.setId(currentUser.get().getId());
            userLogin.setEmail(currentUser.get().getEmail());
            userLogin.setName(currentUser.get().getName());
            userLogin.setRole(currentUser.get().getRole());
            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok(userGetAccount);
    }
    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token",defaultValue = "abc") String refreshToken
    ) throws IdInvalidException {
        if(refreshToken.equals("abc")) {
            throw new IdInvalidException("Bạn không có refreshToken ở cookies");
        }
        //check valiid , giải mã token
        Jwt decodedToken= this.securityUtil.checkRefreshToken(refreshToken);
        String email = decodedToken.getSubject();
//        check user by token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if(currentUser == null) {
            throw new IdInvalidException("RefreshToken không hơp lệ");
        }
//        issue new token/set refresh token as cookies
        ResLoginDTO restLoginDTO = new ResLoginDTO();
//        lấy API thông tin người nguười login
        Optional<User> currentUserDB = this.userService.handleGetUserByUserName(email);
        ResLoginDTO.UserLogin userLogin;
        if (currentUserDB.isPresent()) {
            userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.get().getId(),
                    currentUserDB.get().getEmail(),
                    currentUserDB.get().getName(),
                    currentUserDB.get().getRole()
            );
            restLoginDTO.setUser(userLogin);
        }
        //    create accsessToken
        String access_token = this.securityUtil.createAccessToken(email,restLoginDTO);
        restLoginDTO.setAccessToken(access_token);

//        create refresh token(email,dto)
        String new_refresh_token = this.securityUtil.createRefreshToken(email, restLoginDTO);
//        update user
        this.userService.updateUserToken(new_refresh_token, email);

//        set cookies
        ResponseCookie resCookie=ResponseCookie.from("refresh_token1",new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,resCookie.toString())
                .body(restLoginDTO);
    }
//    logout
    @PostMapping("/auth/logout")
    @ApiMessage("Logiut success")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()?
                SecurityUtil.getCurrentUserLogin().get() : "";
        if(email.equals("")){
            throw new IdInvalidException("Access token không hợp le");
        }
//        update refreshToken == null
        this.userService.updateUserToken(null, email);
//        remove refresh token cookie
        ResponseCookie deleSpringCookie = ResponseCookie.from("refresh_token",null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,deleSpringCookie.toString()).body(null);
    }
    @PostMapping("/auth/register")
    @ApiMessage("Register a  new user")
    public ResponseEntity<ResCreateUserDTO> register (@Valid @RequestBody User reqUser) throws IdInvalidException {
        boolean isEmailexist = this.userService.isEmailExist(reqUser.getEmail());
        if(isEmailexist){
            throw  new IdInvalidException("Eamil đã tồn tại, vui lòng sử dụng email khác");
        }
        String hassPassword = this.passwordEncoder.encode(reqUser.getPassword());
        reqUser.setPassword(hassPassword);
        Role hr = roleService.findByName("USER");
        reqUser.setRole(hr);
        User registerUser = this.userService.saveUser(reqUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToRestCreateUserDTO(registerUser));
    }
    @PostMapping("/auth/registerHR")
    @ApiMessage("Create a new employee")
    public ResponseEntity<ResCreateUserDTO> registerHR(@Valid @RequestBody User reqUser) throws IdInvalidException {
        boolean isEmailexist = this.userService.isEmailExist(reqUser.getEmail());
        if(isEmailexist){
            throw  new IdInvalidException("Eamil đã tồn tại, vui lòng sử dụng email khác");
        }
        String hassPassword = this.passwordEncoder.encode(reqUser.getPassword());
        reqUser.setPassword(hassPassword);
       Role hr = roleService.findByName("HR");
       reqUser.setRole(hr);
       User registerUser = this.userService.saveUser(reqUser);
       return ResponseEntity.ok().body(this.userService.convertToRestCreateUserDTO(registerUser));
    }
    @PostMapping("/auth/outbound/authentication")
    @ApiMessage("login with gg")
    public ResponseEntity<ResLoginDTO> outboundAuthentication( @RequestParam("code") String code) {

        var result = securityUtil.outboundAuthenticate(code);

        String refreshToken = securityUtil.createRefreshToken(result.getUser().getEmail(), result);
        log.info("outbound authentication refresh token : {}", refreshToken);
        this.userService.updateUserToken(refreshToken, result.getUser().getEmail());
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,resCookie.toString())
                .body(result);
    }
}
