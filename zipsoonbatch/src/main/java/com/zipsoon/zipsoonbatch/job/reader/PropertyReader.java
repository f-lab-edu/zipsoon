package com.zipsoon.zipsoonbatch.job.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class PropertyReader implements ItemReader<NaverResponseDto.ArticleDto> {
    private static final String HARD_CODED_DONG_CODE = "1111018000";

    private final NaverClient naverClient;
    private NaverResponseDto.ArticleDto[] currentArticles;
    private int currentArticleIndex;
    private int currentPage;
    private boolean hasMore;

    public PropertyReader(NaverClient naverClient) {
        this.naverClient = naverClient;
        this.currentPage = 0;
        this.currentArticleIndex = 0;
        this.hasMore = true;
    }

    @Override
    public NaverResponseDto.ArticleDto read() {
        if (currentArticles == null || currentArticleIndex >= currentArticles.length) {
            if (!hasMore) {
                return null;
            }
            currentPage++;
            fetchNextPage();
        }

        if (currentArticles == null || currentArticles.length == 0) {
            return null;
        }

        return currentArticles[currentArticleIndex++];
    }

    private void fetchNextPage() {
        log.info("Fetching page {} for dongCode: {}", currentPage, HARD_CODED_DONG_CODE);

        NaverResponseDto response = naverClient.get(HARD_CODED_DONG_CODE, currentPage);
        currentArticles = response.getArticleList();
        currentArticleIndex = 0;
        hasMore = response.isMoreData();

        log.info("Fetched {} articles. More data available: {}", currentArticles.length, hasMore);
    }
}
