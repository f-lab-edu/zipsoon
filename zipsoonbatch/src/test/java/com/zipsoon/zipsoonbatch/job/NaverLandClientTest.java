package com.zipsoon.zipsoonbatch.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.zipsoonbatch.job.reader.NaverArticleResponseDto;
import com.zipsoon.zipsoonbatch.job.reader.NaverLandClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class NaverLandClientTest {

    @Autowired
    private NaverLandClient naverLandClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("네이버 부동산 API로부터 매물 목록을 정상적으로 조회할 수 있다")
    void getArticlesSuccess() throws JsonProcessingException {
        // given
        String cortarNo = "1111018000"; // 서울시 종로구 효자동
        int page = 1;

        // when
        NaverArticleResponseDto response = naverLandClient.getArticles(cortarNo, page);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getArticleList()).isNotEmpty();

        NaverArticleResponseDto.ArticleDto firstArticle = response.getArticleList()[0];
        assertThat(firstArticle.getArticleNo()).isNotNull();
        assertThat(firstArticle.getLatitude()).isNotNull();
        assertThat(firstArticle.getLongitude()).isNotNull();

        // Log pretty printed response
        log.info("\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
}