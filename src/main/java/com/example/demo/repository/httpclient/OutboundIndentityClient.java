package com.example.demo.repository.httpclient;


import com.example.demo.dto.request.ExchangeTokenRequest;
import com.example.demo.dto.response.ExchangeTokenResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "outbound-identity",url = "https://oauth2.googleapis.com")
public interface OutboundIndentityClient {
    @PostMapping(value = "/token",produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
