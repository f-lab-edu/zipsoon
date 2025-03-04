package com.zipsoon.batch.infrastructure.external.naver;

import com.zipsoon.batch.infrastructure.external.naver.vo.NaverLandResponseVO;
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
public class NaverLandClient {

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
            backoff = @Backoff(delay = 2000)
    )
    public NaverLandResponseVO get(String cortarNo, int page) throws SkipException {
        try {
            ResponseEntity<NaverLandResponseVO> response = restTemplate.exchange(
                buildUrl(cortarNo, page),
                HttpMethod.GET,
                buildHttpEntity(),
                NaverLandResponseVO.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Failed to retrieve data from Naver API");
            }
            
            NaverLandResponseVO responseBody = response.getBody();
            if (responseBody.articleList().length == 0) {
                log.info("Received empty articleList from Naver API for cortarNo: {}, page: {}", cortarNo, page);
            }

            return responseBody;
        } catch (RestClientException e) {
            log.error("API error for cortarNo: {}, page: {}", cortarNo, page, e);
            return new NaverLandResponseVO(false, cortarNo, new NaverLandResponseVO.NaverLandResponseArticle[0]);
        }
    }

    private String buildUrl(String cortarNo, int page) {
        // 조회하고자 하는 매물 쿼리 파라미터 (네이버페이 부동산 기준)
        // APT: 아파트     PRE: 분양권             ABYG: 아파트분양권
        // JGC: 재건축     VL: 빌라/연립/다세대      DDDGG: 단독/다가구
        // JWJT: 전원주택   SGJT: 상가주택          HOJT: 한옥주택
        // OPST: 오피스텔   OBYG: 오피스텔분양권      GM: 고시원
        // OR: 원룸
        final String ESTATE_TYPE_PARAM_KEY = "realEstateType";
        final String ESTATE_TYPE_PARAM_VALUE = "APT:PRE:ABYG:JGC:VL:DDDGG:JWJT:SGJT:HOJT:OPST:OBYG:GM:OR";

        // 조회하고자 하는 거래유형 (네이버페이 부동산 기준)
        // B1: 전세       B2: 월세      B3: 단기임대
        final String TRADE_TYPE_PARAM_KEY = "tradeType";
        final String TRADE_TYPE_PARAM_VALUE = "B1:B2:B3";

        return UriComponentsBuilder
              .fromUriString(baseUrl + "/articles")
              .queryParam("cortarNo", cortarNo)
              .queryParam("page", page)
              .queryParam("sameAddressGroup", true)
              .queryParam(ESTATE_TYPE_PARAM_KEY, ESTATE_TYPE_PARAM_VALUE)
              .queryParam(TRADE_TYPE_PARAM_KEY, TRADE_TYPE_PARAM_VALUE)
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
