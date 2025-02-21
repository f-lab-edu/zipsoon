package com.zipsoon.batch.infra.naver;

import com.zipsoon.batch.infra.naver.dto.NaverClientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
    public NaverClientDto get(String cortarNo, int page) throws SkipException {
        try {
            ResponseEntity<NaverClientDto> response = restTemplate.exchange(
                buildUrl(cortarNo, page),
                HttpMethod.GET,
                buildHttpEntity(),
                NaverClientDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Failed to retrieve data from Naver API");
            }
            return response.getBody();
        } catch (RestClientException e) {
            log.error("API error, returning null or fallback response", e);
            throw e;
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
