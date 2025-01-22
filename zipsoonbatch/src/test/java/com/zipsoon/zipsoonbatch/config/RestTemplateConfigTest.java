package com.zipsoon.zipsoonbatch.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class RestTemplateConfigTest {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${naver.land.auth-token}")
    private String naverAuthToken;

    @Test
    @DisplayName("RestTemplate이 네이버 부동산 API를 정상적으로 호출할 수 있어야 한다")
    void restTemplateCanMakeRequest() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        System.out.println("naverAuthToken: " + naverAuthToken);
        headers.set("authorization", "Bearer " + naverAuthToken);
        headers.set("referer", "https://new.land.naver.com");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "https://new.land.naver.com/api/articles?cortarNo=1111018000&page=1",
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        Object json = objectMapper.readValue(response.getBody(), Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

        log.info("Response from Naver Land API:");
        log.info("\n{}", prettyJson);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
