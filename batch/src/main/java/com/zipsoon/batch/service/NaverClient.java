package com.zipsoon.batch.service;

import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.exception.NaverApiException;
import com.zipsoon.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverClient {

    private final RestTemplate restTemplate;

    @Value("${naver.land.base-url}")
    private String baseUrl;

    @Value("${naver.land.auth-token}")
    private String authToken;

    @Retryable(
        retryFor = {
            // Allow
            RestClientException.class,          // network
            HttpServerErrorException.class,     // 5xx
            ResourceAccessException.class       // timeout

            // Ignore
//            HttpClientErrorException          // 400~499
//            IllegalArgumentException          // bad request
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public NaverResponseDto get(String cortarNo, int page) {
        try {
            ResponseEntity<NaverResponseDto> response = restTemplate.exchange(
                buildUrl(cortarNo, page),
                HttpMethod.GET,
                buildHttpEntity(),
                NaverResponseDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new NaverApiException(
                    ErrorCode.EXTERNAL_API_ERROR, "Naver API call failed.");
            }
            return response.getBody();
        } catch (RestClientException e) {
            throw new NaverApiException(ErrorCode.EXTERNAL_API_ERROR,
                "Failed to connect to Naver API",
                Map.of("error", e.getMessage())
            );
        }
    }

    private String buildUrl(String cortarNo, int page) {
        return UriComponentsBuilder
              .fromUriString(baseUrl + "/articles")
              .queryParam("cortarNo", cortarNo)
              .queryParam("page", page)
              .queryParam("sameAddressGroup", true)
              .build()
              .toUriString();
    }

    private HttpEntity<?> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + authToken);
        headers.set("referer", "https://new.land.naver.com");
        return new HttpEntity<>(headers);
    }

}
