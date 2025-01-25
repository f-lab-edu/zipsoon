package com.zipsoon.zipsoonbatch.job.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class PropertyReader implements ItemReader<NaverArticleResponseDto.ArticleDto> {
    private static final String HARD_CODED_DONG_CODE = "1111018000";

    private final NaverLandClient naverLandClient;
    private NaverArticleResponseDto.ArticleDto[] currentArticles;
    private int currentArticleIndex;
    private int currentPage;
    private boolean hasMore;

    public PropertyReader(NaverLandClient naverLandClient) {
        this.naverLandClient = naverLandClient;
        this.currentPage = 0;
        this.currentArticleIndex = 0;
        this.hasMore = true;
    }

    @Override
    public NaverArticleResponseDto.ArticleDto read() {
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

        NaverArticleResponseDto response = naverLandClient.getArticles(HARD_CODED_DONG_CODE, currentPage);
        currentArticles = response.getArticleList();
        currentArticleIndex = 0;
        hasMore = response.isMoreData();

        log.info("Fetched {} articles. More data available: {}", currentArticles.length, hasMore);
    }
}
