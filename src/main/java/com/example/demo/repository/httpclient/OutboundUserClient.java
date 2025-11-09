package com.example.demo.repository.httpclient;


import com.example.demo.dto.request.ExchangeTokenRequest;
import com.example.demo.dto.response.ExchangeTokenResponse;
import com.example.demo.dto.response.OutboundUserResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-user",url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth2/v3/userinfo")
    OutboundUserResponse getUserInfomation(@RequestHeader("Authorization") String authorization);
}
