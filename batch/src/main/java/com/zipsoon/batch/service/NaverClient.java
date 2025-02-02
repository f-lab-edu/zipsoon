package com.zipsoon.batch.service;

import com.zipsoon.batch.dto.NaverResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public NaverResponseDto get(String cortarNo, int page) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
              .fromUriString(baseUrl + "/articles")
              .queryParam("cortarNo", cortarNo)
              .queryParam("page", page)
              .queryParam("sameAddressGroup", true)
              .build()
              .toUriString();

        ResponseEntity<NaverResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                NaverResponseDto.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to fetch articles from Naver Land API. Status Code: {}", response.getStatusCode());
            throw new RuntimeException("Failed to fetch articles from Naver Land API. Status Code: " + response.getStatusCode());
        }

        log.info("Naver API Response: {}", response.getBody());
        return response.getBody();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "Bearer " + authToken);
        headers.set("referer", "https://new.land.naver.com");
        return headers;
    }
}
