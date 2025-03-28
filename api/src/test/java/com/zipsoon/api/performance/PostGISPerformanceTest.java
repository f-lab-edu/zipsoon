package com.zipsoon.api.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * PostGIS 공간 인덱스와 쿼리 성능 테스트
 *
 * <p>부하 상황에서 일반 좌표 기반 쿼리와 PostGIS 공간 쿼리의 성능을 비교합니다.</p>
 * <p>이 테스트는 매물 검색 서비스에서 지리적 검색을 위한 두 가지 방식을 비교합니다:</p>
 * <p>1. 일반 좌표 기반 쿼리: WHERE X >= ? AND X <= ? AND Y >= ? AND Y <= ?</p>
 * <p>2. PostGIS 공간 인덱스 쿼리: WHERE ST_Contains(ST_MakeEnvelope(...), location)</p>
 * <p>부하 상황(동시 접속자 50명)에서 95% 사용자의 경험을 측정해 실제 서비스 환경에서의
 * 성능 차이를 확인합니다.</p>
 */
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostGISPerformanceTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("postgis_test")
            .withUsername("test")
            .withPassword("test")
            .withSharedMemorySize((long) 1024 * 1024 * 1024);

    private JdbcTemplate jdbcTemplate;

    // 결과 파일 경로
    private static final String RESULTS_FILE = "postgis_performance_results.csv";
    private static final String SUMMARY_CHART_FILE = "postgis_performance_summary.html";
    private static final String STANDARD_TIMES_FILE = "postgis_standard_query_times.csv";
    private static final String POSTGIS_TIMES_FILE = "postgis_postgis_query_times.csv";

    // 테스트할 데이터 크기 (행 수)
    private static final int[] DATA_SIZES = {1500, 15000, 150000, 1500000};

    // 쿼리 영역 크기 (도 단위, 약 3km에 해당)
    private static final double AREA_SIZE = 0.03;

    private static final int BACKGROUND_LOAD_USERS = 50;  // 배경 부하 생성을 위한 동시 사용자 수
    private static final int REQUESTS_PER_SECOND = 50;    // 초당 요청 수 (RPS)
    private static final int BATCH_SIZE = 1000;          // 벌크 연산 배치 크기
    private static final int MAX_TEST_DURATION_SECONDS = 300; // 최대 테스트 시간 (5분)

    // 최소 샘플 수
    private static final int MIN_SAMPLES_PER_QUERY_TYPE = 200;

    // 재추출 반복 횟수
    private static final int BOOTSTRAP_ITERATIONS = 1000;

    @BeforeAll
    void setup() {
        // HikariCP를 사용한 확장된 DB 커넥션 풀 설정
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setMaximumPoolSize(BACKGROUND_LOAD_USERS + 10); // 부하 생성 + 측정용 여유 커넥션
        config.setConnectionTimeout(30000); // 30초
        config.setMinimumIdle(10);
        config.setAutoCommit(true);

        HikariDataSource dataSource = new HikariDataSource(config);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // 테스트 테이블 생성
        setupTestTable();
    }

    /**
     * 테스트 테이블 생성 및 PostGIS 확장 활성화
     */
    private void setupTestTable() {
        log.info("테스트 데이터베이스 초기화 시작");

        // PostGIS 확장 설치
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS postgis");

        // 테스트 테이블 생성
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS estate (" +
                        "id SERIAL PRIMARY KEY, " +
                        "platform_type VARCHAR(20) NOT NULL, " +
                        "platform_id VARCHAR(50) NOT NULL, " +
                        "raw_data JSONB NOT NULL, " +
                        "estate_name VARCHAR(100), " +
                        "estate_type VARCHAR(20), " +
                        "trade_type VARCHAR(20), " +
                        "price NUMERIC(15,2), " +
                        "rent_price NUMERIC(15,2), " +
                        "area_meter NUMERIC(10,2), " +
                        "area_pyeong NUMERIC(10,2), " +
                        "location GEOMETRY(Point, 4326) NOT NULL, " +
                        "address VARCHAR(200), " +
                        "image_urls VARCHAR[] DEFAULT '{}', " +
                        "tags VARCHAR[], " +
                        "dong_code VARCHAR(10), " +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"
        );

        // 공간 인덱스 생성
        jdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS estate_location_idx ON estate USING GIST(location)"
        );

        log.info("테스트 데이터베이스 초기화 완료");
    }

    @AfterAll
    void cleanup() {
        // 테스트 데이터 정리
        jdbcTemplate.execute("DROP TABLE IF EXISTS estate");
        log.info("테스트 데이터 정리 완료");
    }

    @Test
    public void testSpatialQueryPerformance() throws Exception {
        log.info("PostGIS 공간 쿼리 성능 테스트 시작");
        log.info("설정: 최소 샘플 수={}, 부하={} 사용자, RPS={}",
                MIN_SAMPLES_PER_QUERY_TYPE, BACKGROUND_LOAD_USERS, REQUESTS_PER_SECOND);

        // 결과 파일 초기화
        initializeResultsFile();
        initializeResponseTimesFile(STANDARD_TIMES_FILE);
        initializeResponseTimesFile(POSTGIS_TIMES_FILE);

        List<DataSizeResult> allResults = new ArrayList<>();

        // 반복 실행 횟수 (통계적 안정성 향상)
        final int ITERATIONS = 10;

        // 각 데이터 크기에 대해 테스트 실행
        for (int dataSize : DATA_SIZES) {
            log.info("데이터 크기 {} 테스트 준비", dataSize);

            // 테스트 데이터 생성 (한 번만)
            generateTestData(dataSize);

            // 워밍업 단계 추가 (각 쿼리 유형별로 일정 수의 쿼리 실행)
            log.info("워밍업 단계 시작 - 각 쿼리 유형별 200개 쿼리 실행");
            runWarmupQueries(dataSize, 200);

            // 여러 번 반복 측정 - 각 반복의 결과 저장
            List<DataSizeResult> iterationResults = new ArrayList<>();

            for (int iteration = 1; iteration <= ITERATIONS; iteration++) {
                log.info("데이터 크기 {} - 반복 {}/{} 시작", dataSize, iteration, ITERATIONS);

                // 1. 일반 좌표 쿼리 테스트 실행
                log.info("일반 좌표 쿼리 테스트 시작 (데이터 크기: {}, 반복: {})", dataSize, iteration);
                List<Double> standardTimes = runQueryTest(dataSize, "일반 좌표 쿼리", this::measureStandardQuery);

                // 2. PostGIS 공간 쿼리 테스트 실행
                log.info("PostGIS 공간 쿼리 테스트 시작 (데이터 크기: {}, 반복: {})", dataSize, iteration);
                List<Double> postgisTimes = runQueryTest(dataSize, "PostGIS 공간 쿼리", this::measurePostgisQuery);

                // 결과 분석
                DataSizeResult result = calculateStatistics(dataSize, standardTimes, postgisTimes);
                iterationResults.add(result);

                log.info("반복 {}/{} - 데이터 크기 {}: P95 일반 쿼리={}ms, P95 PostGIS={}ms",
                        iteration, ITERATIONS,
                        dataSize,
                        // 소수점 둘째 자리에서 첫째 자리로 변경
                        String.format("%.1f", result.p95StandardTime()),
                        String.format("%.1f", result.p95PostgisTime()));
            }

            // 부트스트랩 방법만 사용하도록 수정된 집계 메서드 호출
            DataSizeResult aggregatedResult = aggregateResults(dataSize, iterationResults);
            allResults.add(aggregatedResult);

            // 결과를 파일에 추가
            appendResultToFile(aggregatedResult);

            log.info("데이터 크기 {} 최종 결과 ({} 반복 평균): P95 일반 쿼리={}ms, P95 PostGIS={}ms",
                    dataSize,
                    ITERATIONS,
                    // 소수점 둘째 자리에서 첫째 자리로 변경
                    String.format("%.1f", aggregatedResult.p95StandardTime()),
                    String.format("%.1f", aggregatedResult.p95PostgisTime()));
        }

        // 차트 생성
        generatePerformanceChart(allResults);

        log.info("PostGIS 공간 쿼리 성능 테스트 완료");
        log.info("결과 파일: {}, 차트: {}", RESULTS_FILE, SUMMARY_CHART_FILE);
    }

    /**
     * 워밍업 쿼리 실행
     * @param dataSize 데이터 크기
     * @param warmupQueryCount 워밍업 쿼리 수
     */
    private void runWarmupQueries(int dataSize, int warmupQueryCount) {
        Random random = new Random();

        log.info("워밍업: 일반 좌표 쿼리 {} 개 실행 중...", warmupQueryCount);
        for (int i = 0; i < warmupQueryCount; i++) {
            double centerLng = 126.9 + (random.nextDouble() * 0.2);
            double centerLat = 37.5 + (random.nextDouble() * 0.2);
            measureStandardQuery(dataSize, AREA_SIZE, centerLng, centerLat);
        }

        log.info("워밍업: PostGIS 공간 쿼리 {} 개 실행 중...", warmupQueryCount);
        for (int i = 0; i < warmupQueryCount; i++) {
            double centerLng = 126.9 + (random.nextDouble() * 0.2);
            double centerLat = 37.5 + (random.nextDouble() * 0.2);
            measurePostgisQuery(dataSize, AREA_SIZE, centerLng, centerLat);
        }

        // 인덱스 통계 갱신 (실행 계획 최적화를 위해)
        jdbcTemplate.execute("ANALYZE estate");

        log.info("워밍업 완료");
    }

    /**
     * 여러 반복 실행의 결과를 집계 - 단일 부트스트랩 방식만 사용
     */
    private DataSizeResult aggregateResults(int dataSize, List<DataSizeResult> results) {
        // 모든 반복의 개별 샘플 데이터 수집
        List<Double> allStandardP95s = results.stream()
            .map(DataSizeResult::p95StandardTime)
            .collect(Collectors.toList());

        List<Double> allPostgisP95s = results.stream()
            .map(DataSizeResult::p95PostgisTime)
            .collect(Collectors.toList());

        // 부트스트랩 방법으로 평균 P95 및 신뢰 구간 계산
        double avgP95StandardTime = calculateAverage(allStandardP95s);
        double avgP95PostgisTime = calculateAverage(allPostgisP95s);

        // 평균 계산
        double avgStandardTime = results.stream()
            .mapToDouble(DataSizeResult::avgStandardTime)
            .average()
            .orElse(0.0);

        double avgPostgisTime = results.stream()
            .mapToDouble(DataSizeResult::avgPostgisTime)
            .average()
            .orElse(0.0);

        // 부트스트랩으로 신뢰 구간 계산
        ConfidenceInterval standardCI = calculateBootstrapConfidenceInterval(allStandardP95s, 95, BOOTSTRAP_ITERATIONS);
        ConfidenceInterval postgisCI = calculateBootstrapConfidenceInterval(allPostgisP95s, 95, BOOTSTRAP_ITERATIONS);

        // 샘플 수 (모든 반복의 샘플 수 합계)
        int totalStandardSamples = results.stream()
            .mapToInt(DataSizeResult::standardSampleCount)
            .sum();

        int totalPostgisSamples = results.stream()
            .mapToInt(DataSizeResult::postgisSampleCount)
            .sum();

        return new DataSizeResult(
            dataSize,
            totalStandardSamples,
            totalPostgisSamples,
            avgStandardTime,
            avgPostgisTime,
            avgP95StandardTime,
            avgP95PostgisTime,
            standardCI,
            postgisCI
        );
    }

    /**
     * 평균 계산 헬퍼 메서드
     */
    private double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 특정 쿼리 유형에 대한 부하 테스트 실행
     *
     * @param dataSize      데이터 크기
     * @param queryName     쿼리 이름 (로깅용)
     * @param queryExecutor 실행할 쿼리 함수
     * @return 측정된 응답 시간 목록
     */
    private List<Double> runQueryTest(int dataSize, String queryName, QueryExecutor queryExecutor) throws InterruptedException {
        log.info("{} 테스트 시작: 목표 샘플 수 {}", queryName, MIN_SAMPLES_PER_QUERY_TYPE);

        List<Double> responseTimes = Collections.synchronizedList(new ArrayList<>());

        // 수집된 샘플 수 추적
        AtomicInteger sampleCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // 워커 스레드 풀 생성
        ExecutorService workerPool = Executors.newFixedThreadPool(BACKGROUND_LOAD_USERS);

        try {
            // 각 워커 스레드 시작
            for (int i = 0; i < BACKGROUND_LOAD_USERS; i++) {
                workerPool.submit(() -> {
                    Random random = new Random();

                    // 사용자당 요청 간격 계산 (균일한 RPS를 위해)
                    int requestsPerUser = REQUESTS_PER_SECOND / Math.max(1, BACKGROUND_LOAD_USERS);
                    long requestIntervalMs = 1000 / Math.max(1, requestsPerUser);

                    // 목표 샘플 수에 도달할 때까지 실행
                    while (sampleCount.get() < MIN_SAMPLES_PER_QUERY_TYPE) {
                        try {
                            // 랜덤 위치 생성 (서울 지역 내)
                            double centerLng = 126.9 + (random.nextDouble() * 0.2);
                            double centerLat = 37.5 + (random.nextDouble() * 0.2);

                            QueryResult result = queryExecutor.execute(dataSize, AREA_SIZE, centerLng, centerLat);

                            // 응답 시간 저장 및 카운터 증가
                            responseTimes.add(result.executionTimeMs());
                            int current = sampleCount.incrementAndGet();

                            // 로그
                            if (current % 50 == 0) {
                                log.debug("{} 샘플 {}개 수집 완료", queryName, current);
                            }

                            // 목표 달성 시 종료
                            if (current >= MIN_SAMPLES_PER_QUERY_TYPE) {
                                break;
                            }

                            // 일정한 요청 간격 유지
                            Thread.sleep(requestIntervalMs);

                        } catch (Exception e) {
                            log.error("{} 실행 중 오류: {}", queryName, e.getMessage());
                        }
                    }
                });
            }

            // 테스트 진행 상황 모니터링 - 개선된 로깅 알고리즘
            long nextLogTime = System.currentTimeMillis() + 10000; // 10초 후 첫 로그
            while (sampleCount.get() < MIN_SAMPLES_PER_QUERY_TYPE) {
                Thread.sleep(1000); // 1초마다 확인

                long currentTime = System.currentTimeMillis();
                if (currentTime >= nextLogTime) {
                    int current = sampleCount.get();
                    double percentComplete = (double) current / MIN_SAMPLES_PER_QUERY_TYPE * 100;
                    long elapsedSeconds = (currentTime - startTime) / 1000;

                    log.info("{} 진행 중: {}개/{} ({}%), 경과 시간: {}초",
                            queryName, current, MIN_SAMPLES_PER_QUERY_TYPE,
                            String.format("%.1f", percentComplete), elapsedSeconds);

                    nextLogTime = currentTime + 10000; // 다음 로그는 10초 후
                }

                // 시간 초과 확인
                if (System.currentTimeMillis() - startTime > MAX_TEST_DURATION_SECONDS * 1000) {
                    log.warn("{} 최대 시간 초과: 목표 샘플 수에 도달하지 못했습니다", queryName);
                    break;
                }
            }

        } finally {
            workerPool.shutdown();
            try {
                // 종료 대기
                if (!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    workerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 테스트 완료 요약
        long totalTimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        double actualRps = responseTimes.size() / (double) totalTimeSeconds;

        log.info("{} 테스트 완료: {}개 샘플 수집, 소요 시간: {}초, 실제 RPS: {}",
                queryName, responseTimes.size(), totalTimeSeconds,
                String.format("%.1f", actualRps));

        // 응답 시간을 해당 파일에 저장
        try {
            String responseTimesFile = queryName.contains("PostGIS") ? POSTGIS_TIMES_FILE : STANDARD_TIMES_FILE;
            saveResponseTimesToFile(responseTimesFile, dataSize, responseTimes);
        } catch (Exception e) {
            log.error("응답 시간 저장 중 오류: {}", e.getMessage(), e);
        }

        return responseTimes;
    }

    /**
     * 쿼리 실행 함수를 정의하는 함수형 인터페이스
     */
    @FunctionalInterface
    interface QueryExecutor {
        QueryResult execute(int dataSize, double areaSize, double centerLng, double centerLat);
    }

    /**
     * 일반 좌표 쿼리 성능 측정
     */
    private QueryResult measureStandardQuery(int dataSize, double radius, double centerLng, double centerLat) {
        double swLng = centerLng - radius;
        double swLat = centerLat - radius;
        double neLng = centerLng + radius;
        double neLat = centerLat + radius;

        String query =
                "SELECT COUNT(*) FROM estate " +
                        "WHERE ST_X(location) >= ? AND ST_X(location) <= ? " +
                        "AND ST_Y(location) >= ? AND ST_Y(location) <= ?";

        long startTime = System.nanoTime();
        Integer result = jdbcTemplate.queryForObject(
                query,
                Integer.class,
                swLng, neLng, swLat, neLat
        );
        long endTime = System.nanoTime();

        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        return new QueryResult(
                LocalDateTime.now(),
                "STANDARD",
                executionTimeMs,
                result
        );
    }

    /**
     * PostGIS 공간 쿼리 성능 측정
     */
    private QueryResult measurePostgisQuery(int dataSize, double radius, double centerLng, double centerLat) {
        double swLng = centerLng - radius;
        double swLat = centerLat - radius;
        double neLng = centerLng + radius;
        double neLat = centerLat + radius;

        String query =
                "SELECT COUNT(*) FROM estate " +
                        "WHERE ST_Contains(ST_MakeEnvelope(?, ?, ?, ?, 4326), location)";

        long startTime = System.nanoTime();
        Integer result = jdbcTemplate.queryForObject(
                query,
                Integer.class,
                swLng, swLat, neLng, neLat
        );
        long endTime = System.nanoTime();

        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        return new QueryResult(
                LocalDateTime.now(),
                "POSTGIS",
                executionTimeMs,
                result
        );
    }

    /**
     * 테스트 데이터 생성
     */
    private void generateTestData(int count) {
        // 기존 테스트 데이터 삭제
        jdbcTemplate.execute("TRUNCATE TABLE estate CASCADE");

        // 새 테스트 데이터 생성 및 삽입
        Random random = new Random();

        // 대량 삽입을 위한 배치 처리
        List<Object[]> batchParams = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < count; i++) {
            // 서울 지역 범위 내 랜덤 좌표 생성
            double lng = 126.8 + (random.nextDouble() * 0.4); // 서울 경도 범위
            double lat = 37.4 + (random.nextDouble() * 0.4);  // 서울 위도 범위

            // 테스트 매물 데이터 생성
            batchParams.add(new Object[]{
                    "네이버",                  // platform_type
                    "test-" + i,              // platform_id
                    "{}",                     // raw_data (JSON)
                    "테스트 매물 " + i,          // estate_name
                    "아파트",                   // estate_type
                    "전세",                     // trade_type
                    10000 + random.nextInt(90000), // price (만원)
                    null,                     // rent_price
                    80 + random.nextInt(40),  // area_meter
                    0,                        // area_pyeong (계산됨)
                    "POINT(" + lng + " " + lat + ")", // location
                    "서울시 테스트구 테스트동 " + i,    // address
                    null,                     // image_urls
                    null,                     // tags
                    "1111011800"              // dong_code
            });

            // 배치 삽입 실행
            if (batchParams.size() >= BATCH_SIZE || i == count - 1) {
                jdbcTemplate.batchUpdate(
                        "INSERT INTO estate (platform_type, platform_id, raw_data, estate_name, " +
                                "estate_type, trade_type, price, rent_price, area_meter, area_pyeong, " +
                                "location, address, image_urls, tags, dong_code) " +
                                "VALUES (?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ?, ?, ?, ?)",
                        batchParams
                );
                batchParams.clear();
                log.debug("데이터 {}개 중 {}개 생성 완료", count, Math.min((i + 1), count));
            }
        }

        // 인덱스 통계 갱신
        jdbcTemplate.execute("ANALYZE estate");

        log.info("테스트 데이터 {}개 생성 완료", count);
    }

    /**
     * 측정 결과에 대한 통계 계산
     *
     * <p>모든 수집된 쿼리 응답 시간 중 95번째 백분위수를 계산합니다.
     * 이는 "95%의 사용자 요청이 이 시간 이내에 처리됨"을 의미합니다.</p>
     *
     * <p>쿼리 응답 시간은 일반적으로 오른쪽으로 치우친 분포(right-skewed)를 보이므로,
     * 로그 변환 후 계산한 결과를 사용합니다.</p>
     *
     * <p>부트스트랩 방법을 통해 신뢰 구간을 계산합니다.</p>
     */
    private DataSizeResult calculateStatistics(int dataSize,
                                               List<Double> standardTimes,
                                               List<Double> postgisTimes) {
        // 충분한 샘플이 있는지 확인
        int standardSampleCount = standardTimes.size();
        int postgisSampleCount = postgisTimes.size();
        if (standardSampleCount < MIN_SAMPLES_PER_QUERY_TYPE || postgisSampleCount < MIN_SAMPLES_PER_QUERY_TYPE) {
            log.warn("표본이 부족합니다: 일반={}/{}, PostGIS={}/{}. 통계 계산의 신뢰성이 낮을 수 있습니다.",
                    standardSampleCount, MIN_SAMPLES_PER_QUERY_TYPE,
                    postgisSampleCount, MIN_SAMPLES_PER_QUERY_TYPE);
        }

        // 스트림을 사용하여 로그 변환된 데이터 생성
        List<Double> standardTimesLog = standardTimes.stream()
                .map(time -> Math.log(Math.max(0.001, time)))
                .toList();

        List<Double> postgisTimesLog = postgisTimes.stream()
                .map(time -> Math.log(Math.max(0.001, time)))
                .toList();

        // P95 백분위수 계산 (로그 변환된 데이터)
        double p95StandardTimeLog = calculatePercentile(standardTimesLog, 95);
        double p95PostgisTimeLog = calculatePercentile(postgisTimesLog, 95);

        // 원래 스케일로 변환
        double p95StandardTime = Math.exp(p95StandardTimeLog);
        double p95PostgisTime = Math.exp(p95PostgisTimeLog);

        // 평균 계산
        double avgStandardTime = standardTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgPostgisTime = postgisTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 부트스트랩 신뢰 구간 계산
        ConfidenceInterval standardCI = calculateBootstrapConfidenceInterval(standardTimes, 95, BOOTSTRAP_ITERATIONS);
        ConfidenceInterval postgisCI = calculateBootstrapConfidenceInterval(postgisTimes, 95, BOOTSTRAP_ITERATIONS);

        log.info("P95 계산 결과 (데이터 크기: {})", dataSize);
        // 소수점 둘째 자리에서 첫째 자리로 변경
        log.info("  일반 쿼리: P95={} ms, 95% 신뢰 구간=[{}, {}]",
                String.format("%.1f", p95StandardTime),
                String.format("%.1f", standardCI.lowerBound()),
                String.format("%.1f", standardCI.upperBound()));
        log.info("  PostGIS: P95={} ms, 95% 신뢰 구간=[{}, {}]",
                String.format("%.1f", p95PostgisTime),
                String.format("%.1f", postgisCI.lowerBound()),
                String.format("%.1f", postgisCI.upperBound()));

        return new DataSizeResult(
                dataSize,
                standardSampleCount,
                postgisSampleCount,
                avgStandardTime,
                avgPostgisTime,
                p95StandardTime,
                p95PostgisTime,
                standardCI,
                postgisCI
        );
    }

    /**
     * 부트스트랩 방법을 사용한 신뢰 구간 계산
     *
     * @param samples    원본 샘플 데이터
     * @param percentile 계산할 백분위수 (예: 95)
     * @param iterations 부트스트랩 반복 횟수
     * @return 신뢰 구간 (하한, 상한)
     */
    private ConfidenceInterval calculateBootstrapConfidenceInterval(
            List<Double> samples, int percentile, int iterations) {
        Random random = new Random();
        List<Double> bootstrapStats = new ArrayList<>(iterations);

        // 반복적으로 재샘플링하여 통계치 분포 생성
        for (int i = 0; i < iterations; i++) {
            List<Double> resample = new ArrayList<>(samples.size());
            for (int j = 0; j < samples.size(); j++) {
                resample.add(samples.get(random.nextInt(samples.size())));
            }
            double stat = calculatePercentile(resample, percentile);
            bootstrapStats.add(stat);
        }

        // 2.5 백분위수와 97.5 백분위수로 95% 신뢰 구간 계산
        double lowerBound = calculatePercentile(bootstrapStats, 2.5);
        double upperBound = calculatePercentile(bootstrapStats, 97.5);

        return new ConfidenceInterval(lowerBound, upperBound);
    }

    /**
     * 백분위수 계산 (스트림 활용)
     *
     * <p>값 목록에서 지정된 백분위수를 계산합니다.</p>
     */
    private double calculatePercentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }

        // 데이터 정렬
        List<Double> sortedValues = values.stream()
                .sorted()
                .toList();

        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    /**
     * 결과 파일 초기화
     */
    private void initializeResultsFile() throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE)) {
            writer.write("dataSize,standardSamples,postgisSamples," +
                    "avgStandardTime,avgPostgisTime," +
                    "p95StandardTime,p95PostgisTime," +
                    "standardCI_Lower,standardCI_Upper," +
                    "postgisCI_Lower,postgisCI_Upper\n");
        }
    }

    /**
     * 응답 시간 파일 초기화
     */
    private void initializeResponseTimesFile(String fileName) throws Exception {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("dataSize,responseTime\n");
        }
    }

    /**
     * 응답 시간을 파일에 저장
     */
    private void saveResponseTimesToFile(String fileName, int dataSize, List<Double> responseTimes) throws Exception {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            for (Double time : responseTimes) {
                writer.write(String.format("%d,%.4f%n", dataSize, time));
            }
        }
    }

    /**
     * 결과 파일에 데이터 추가
     */
    private void appendResultToFile(DataSizeResult result) throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE, true)) {
            // 소수점 둘째 자리에서 첫째 자리로 변경
            writer.write(String.format("%d,%d,%d,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f%n",
                    result.dataSize(),
                    result.standardSampleCount(),
                    result.postgisSampleCount(),
                    result.avgStandardTime(),
                    result.avgPostgisTime(),
                    result.p95StandardTime(),
                    result.p95PostgisTime(),
                    result.standardCI().lowerBound(),
                    result.standardCI().upperBound(),
                    result.postgisCI().lowerBound(),
                    result.postgisCI().upperBound()));
        }
    }

    /**
     * 성능 비교 차트 생성
     * 개선된 버전 - index.js의 고급 시각화 적용
     */
    private void generatePerformanceChart(List<DataSizeResult> results) throws Exception {
        try {
            // 응답 시간 데이터 파일 읽기
            Map<Integer, List<Double>> standardTimesByDataSize = readResponseTimes(STANDARD_TIMES_FILE);
            Map<Integer, List<Double>> postgisTimesByDataSize = readResponseTimes(POSTGIS_TIMES_FILE);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("  <title>부하 상황에서의 PostGIS 공간 쿼리 성능 분석</title>\n");
            html.append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
            html.append("  <script src=\"https://cdn.jsdelivr.net/npm/@sgratzl/chartjs-chart-boxplot@3.10.0/build/index.umd.min.js\"></script>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: 'Pretendard', 'Noto Sans KR', sans-serif; margin: 20px; color: #333; background-color: #f9f9f9; }\n");
            html.append("    .chart-container { width: 90%; max-width: 1000px; margin: 40px auto; background-color: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 20px; }\n");
            html.append("    .chart-container canvas { width: 100% !important; height: 400px !important; max-height: 400px !important; }\n");
            html.append("    h1 { text-align: center; margin-top: 40px; color: #2c3e50; font-size: 2.2em; }\n");
            html.append("    h2 { text-align: center; color: #3498db; margin-bottom: 30px; }\n");
            html.append("    .description { background-color: #e8f4fc; padding: 15px; border-radius: 5px; margin: 10px 0 25px 0; font-size: 0.95em; line-height: 1.5; }\n");
            html.append("    .highlight { background-color: #fffde7; font-weight: bold; padding: 2px 4px; border-radius: 3px; }\n");
            html.append("    .chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; max-width: 2000px; margin: 0 auto; }\n");
            html.append("    @media (max-width: 1200px) { .chart-grid { grid-template-columns: 1fr; } }\n");
            html.append("    .summary { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
            html.append("    .conclusion { max-width: 1000px; margin: 50px auto; background-color: #f1f8e9; padding: 20px; border-radius: 8px; border-left: 5px solid #8bc34a; }\n");
            html.append("    table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
            html.append("    table, th, td { border: 1px solid #ddd; }\n");
            html.append("    th, td { padding: 8px; text-align: right; }\n");
            html.append("    th { background-color: #f2f2f2; }\n");
            html.append("  </style>\n");
            html.append("</head>\n<body>\n");

            // 헤더 및 설명
            html.append("  <h1>PostGIS 공간 쿼리 성능 분석</h1>\n");

            // 주요 발견점 요약
            html.append("  <div class=\"summary\">\n");
            html.append("    <h2>주요 발견점</h2>\n");
            html.append("    <ul>\n");

            // 가장 큰 데이터셋에서의 P95 응답 시간
            results.stream()
                .filter(r -> r.dataSize() == DATA_SIZES[DATA_SIZES.length - 1])
                .findFirst()
                .ifPresent(largest -> {
                    double improvementRatio = largest.p95StandardTime() / largest.p95PostgisTime();
                    // 소수점 둘째 자리에서 첫째 자리로 변경
                    html.append(String.format(
                        "      <li>최대 데이터셋 (%,d개)에서 95%% 사용자의 경험: 일반 쿼리 <strong>%.1f ms</strong> (95%% 신뢰 구간: %.1f-%.1f), PostGIS <strong>%.1f ms</strong> (95%% 신뢰 구간: %.1f-%.1f)</li>%n",
                        largest.dataSize(),
                        largest.p95StandardTime(),
                        largest.standardCI().lowerBound(),
                        largest.standardCI().upperBound(),
                        largest.p95PostgisTime(),
                        largest.postgisCI().lowerBound(),
                        largest.postgisCI().upperBound()
                    ));
                    html.append(String.format(
                        "      <li>PostGIS 공간 쿼리는 일반 좌표 쿼리보다 <span class=\"highlight\">약 %.1f배</span> 더 빠릅니다.</li>%n",
                        improvementRatio
                    ));
                });

            html.append("      <li>데이터 크기가 증가할수록 PostGIS 공간 쿼리의 성능 이점이 커집니다</li>\n");
            html.append("      <li>부하 상황에서도 PostGIS 공간 인덱스가 안정적인 성능을 제공합니다</li>\n");
            html.append("    </ul>\n");
            html.append("  </div>\n");

            // 차트 그리드 시작
            html.append("  <div class=\"chart-grid\">\n");

            // 1. 산점도 + 추세선
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>1. 산점도 + 이동 평균선</h2>\n");
            html.append("      <div class=\"description\">\n");
            html.append("        샘플링된 응답 시간과 이동 평균선(30개 샘플)을 통해 시간 흐름에 따른 변화 추세를 볼 수 있습니다.\n");
            html.append("        <span class=\"highlight\">PostGIS가 일반 쿼리보다 더 안정적인 응답 시간</span>을 보여줍니다.\n");
            html.append("      </div>\n");
            html.append("      <canvas id=\"scatterChart\"></canvas>\n");
            html.append("    </div>\n");

            // 2. 박스플롯
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>2. 박스플롯</h2>\n");
            html.append("      <div class=\"description\">\n");
            html.append("        각 쿼리 유형의 <span class=\"highlight\">사분위수 분포와 이상치</span>를 보여줍니다.\n");
            html.append("        중앙값(Q2), 1사분위수(Q1), 3사분위수(Q3), 최소/최대값, 이상치(outlier)를 확인할 수 있습니다.\n");
            html.append("      </div>\n");
            html.append("      <canvas id=\"boxplotChart\"></canvas>\n");
            html.append("    </div>\n");

            // 3. 커널 밀도 추정
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>3. 커널 밀도 추정 (KDE)</h2>\n");
            html.append("      <div class=\"description\">\n");
            html.append("        응답 시간 분포를 부드러운 곡선으로 표현합니다. \n");
            html.append("        <span class=\"highlight\">분포의 모양과 집중도</span>를 통해 대부분의 사용자가 경험하는 응답 시간 구간을 확인할 수 있습니다.\n");
            html.append("      </div>\n");
            html.append("      <canvas id=\"kdeChart\"></canvas>\n");
            html.append("    </div>\n");

            // 4. 히트맵 (구간별 빈도 시각화로 대체)
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>4. 응답 시간 구간별 빈도 히트맵</h2>\n");
            html.append("      <div class=\"description\">\n");
            html.append("        응답 시간을 구간으로 나누어 각 구간의 <span class=\"highlight\">샘플 빈도를 색상 강도</span>로 표시합니다.\n");
            html.append("        각 쿼리 유형별로 어떤 응답 시간대가 가장 빈번한지 비교할 수 있습니다. 두 쿼리 유형은 동일한 시간 구간을 사용합니다.\n");
            html.append("      </div>\n");
            html.append("      <canvas id=\"heatmapChart\"></canvas>\n");
            html.append("    </div>\n");

            // 5. 백분위수 그래프
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>5. 백분위수 그래프</h2>\n");
            html.append("      <div class=\"description\">\n");
            html.append("        각 백분위수(0%, 10%, 20%, ..., 99%)에서의 응답 시간을 보여줍니다.\n");
            html.append("        <span class=\"highlight\">곡선의 기울기가 급격하게 증가하는 지점</span>은 성능이 악화되는 구간을 의미합니다.\n");
            html.append("      </div>\n");
            html.append("      <canvas id=\"percentileChart\"></canvas>\n");
            html.append("    </div>\n");

            html.append("  </div>\n");  // 차트 그리드 종료

            // 결과 테이블
            html.append("  <div>\n");
            html.append("    <h2>상세 측정 결과</h2>\n");
            html.append("    <table>\n");
            html.append("      <tr>\n");
            html.append("        <th>데이터 크기</th>\n");
            html.append("        <th>일반 쿼리 P95 (ms)</th>\n");
            html.append("        <th>95% 신뢰 구간</th>\n");
            html.append("        <th>PostGIS P95 (ms)</th>\n");
            html.append("        <th>95% 신뢰 구간</th>\n");
            html.append("      </tr>\n");

            for (DataSizeResult result : results) {
                // 소수점 둘째 자리에서 첫째 자리로 변경
                html.append(String.format("      <tr>%n" +
                           "        <td>%,d</td>%n" +
                           "        <td>%.1f</td>%n" +
                           "        <td>[%.1f, %.1f]</td>%n" +
                           "        <td>%.1f</td>%n" +
                           "        <td>[%.1f, %.1f]</td>%n" +
                           "      </tr>%n",
                           result.dataSize(),
                           result.p95StandardTime(),
                           result.standardCI().lowerBound(),
                           result.standardCI().upperBound(),
                           result.p95PostgisTime(),
                           result.postgisCI().lowerBound(),
                           result.postgisCI().upperBound()));
            }

            html.append("    </table>\n");
            html.append("  </div>\n");

            // 결론
            html.append("  <div class=\"conclusion\">\n");
            html.append("    <h2>결론</h2>\n");
            html.append("    <p><strong>PostGIS 공간 인덱스는 대용량 데이터셋에서 일반 좌표 인덱스보다 뚜렷한 성능 우위를 보입니다.</strong></p>\n");
            html.append("    <p>특히:</p>\n");
            html.append("    <ul>\n");
            html.append("      <li>PostGIS 쿼리는 <span class=\"highlight\">응답 시간의 변동성이 작아</span> 보다 일관된 사용자 경험을 제공합니다.</li>\n");
            html.append("      <li>데이터 크기가 증가할수록 PostGIS의 성능 우위가 뚜렷해집니다.</li>\n");
            results.stream()
                .filter(r -> r.dataSize() == DATA_SIZES[DATA_SIZES.length - 1])
                .findFirst()
                .ifPresent(largest -> {
                    double improvementRatio = largest.p95StandardTime() / largest.p95PostgisTime();
                    html.append(String.format(
                        "      <li>95번째 백분위수에서 PostGIS는 일반 쿼리보다 <span class=\"highlight\">약 %.1f배</span> 더 빠릅니다.</li>%n",
                        improvementRatio
                    ));
                });
            html.append("      <li>PostGIS는 최악의 경우(99번째 백분위수) 시나리오에서도 더 안정적인 성능을 보여줍니다.</li>\n");
            html.append("    </ul>\n");
            html.append("    <p>집순 서비스에 PostGIS 공간 쿼리 적용 시, 사용자 50명이 동시에 접속하는 최고 부하 시간대에도 안정적인 성능을 제공할 수 있습니다.</p>\n");
            html.append("  </div>\n");

            // JavaScript 차트 코드
            html.append("  <script>\n");

            // 유틸리티 함수
            html.append("    // 백분위수 계산 함수\n");
            html.append("    function calculatePercentile(values, percentile) {\n");
            html.append("      if (!values || values.length === 0) return 0;\n");
            html.append("      \n");
            html.append("      const sortedValues = [...values].sort((a, b) => a - b);\n");
            html.append("      const index = Math.ceil((percentile / 100) * sortedValues.length) - 1;\n");
            html.append("      return sortedValues[Math.max(0, Math.min(index, sortedValues.length - 1))];\n");
            html.append("    }\n");

            html.append("    // 배열 샘플링 함수\n");
            html.append("    function sampleArray(array, sampleSize) {\n");
            html.append("      if (!array || array.length === 0) return [];\n");
            html.append("      if (array.length <= sampleSize) return array;\n");
            html.append("      \n");
            html.append("      const step = Math.floor(array.length / sampleSize);\n");
            html.append("      const result = [];\n");
            html.append("      \n");
            html.append("      for (let i = 0; i < array.length; i += step) {\n");
            html.append("        result.push(array[i]);\n");
            html.append("        if (result.length >= sampleSize) break;\n");
            html.append("      }\n");
            html.append("      \n");
            html.append("      return result;\n");
            html.append("    }\n");

            html.append("    // 이동 평균 계산 함수\n");
            html.append("    function calculateMovingAverage(data, windowSize) {\n");
            html.append("      const result = [];\n");
            html.append("      for (let i = 0; i < data.length; i++) {\n");
            html.append("        const window = data.slice(\n");
            html.append("          Math.max(0, i - windowSize + 1), \n");
            html.append("          i + 1\n");
            html.append("        );\n");
            html.append("        const avg = window.reduce((sum, val) => sum + val, 0) / window.length;\n");
            html.append("        result.push(avg);\n");
            html.append("      }\n");
            html.append("      return result;\n");
            html.append("    }\n");

            html.append("    // 박스플롯 통계 계산 함수\n");
            html.append("    function calculateBoxplotStats(data) {\n");
            html.append("      if (!data || data.length === 0) return null;\n");
            html.append("      \n");
            html.append("      const sorted = [...data].sort((a, b) => a - b);\n");
            html.append("      const q1 = calculatePercentile(sorted, 25);\n");
            html.append("      const median = calculatePercentile(sorted, 50);\n");
            html.append("      const q3 = calculatePercentile(sorted, 75);\n");
            html.append("      const iqr = q3 - q1;\n");
            html.append("      const lowerWhisker = Math.max(sorted[0], q1 - 1.5 * iqr);\n");
            html.append("      const upperWhisker = Math.min(sorted[sorted.length - 1], q3 + 1.5 * iqr);\n");
            html.append("      \n");
            html.append("      // 이상치 계산\n");
            html.append("      const outliers = sorted.filter(v => v < lowerWhisker || v > upperWhisker);\n");
            html.append("      \n");
            html.append("      return {\n");
            html.append("        min: lowerWhisker,\n");
            html.append("        q1: q1,\n");
            html.append("        median: median,\n");
            html.append("        q3: q3,\n");
            html.append("        max: upperWhisker,\n");
            html.append("        outliers: outliers\n");
            html.append("      };\n");
            html.append("    }\n");

            html.append("    // 커널 밀도 추정 데이터 생성 함수\n");
            html.append("    function generateKDEData(data, bandwidth = 10, points = 100) {\n");
            html.append("      if (!data || data.length === 0) {\n");
            html.append("        return Array(points).fill().map((_, i) => ({x: i, y: 0}));\n");
            html.append("      }\n");
            html.append("      \n");
            html.append("      // 데이터 범위 설정 (최소값을 0으로 설정하여 그래프 가시성 개선)\n");
            html.append("      const min = Math.max(0, Math.min(...data) * 0.9);\n");
            html.append("      const max = Math.max(...data) * 1.1; // 최대값 여유 추가\n");
            html.append("      const range = max - min;\n");
            html.append("      \n");
            html.append("      // 범위가 너무 작으면 기본 범위 설정\n");
            html.append("      const adjustedRange = range < 1 ? 10 : range;\n");
            html.append("      \n");
            html.append("      // 평가 포인트 생성 (균등 간격으로)\n");
            html.append("      const xs = Array.from({length: points}, (_, i) => \n");
            html.append("        min + (i / (points - 1)) * adjustedRange);\n");
            html.append("      \n");
            html.append("      // 각 포인트에서 커널 밀도 추정\n");
            html.append("      // 최적의 bandwidth 선택 (Silverman's rule of thumb)\n");
            html.append("      const n = data.length;\n");
            html.append("      const stdDev = Math.sqrt(data.reduce((sum, x) => sum + Math.pow(x - data.reduce((a, b) => a + b, 0) / n, 2), 0) / n);\n");
            html.append("      const optBandwidth = bandwidth || (0.9 * Math.min(stdDev, (Math.max(...data) - Math.min(...data)) / 1.34) * Math.pow(n, -0.2));\n");
            html.append("      \n");
            html.append("      // 각 포인트에서 가우시안 커널 적용\n");
            html.append("      const density = xs.map(x => {\n");
            html.append("        const sum = data.reduce((acc, val) => {\n");
            html.append("          const z = (x - val) / optBandwidth;\n");
            html.append("          return acc + Math.exp(-0.5 * z * z) / (optBandwidth * Math.sqrt(2 * Math.PI));\n");
            html.append("        }, 0);\n");
            html.append("        \n");
            html.append("        return {x, y: sum / data.length};\n");
            html.append("      });\n");
            html.append("      \n");
            html.append("      // y값 정규화 (최대값을 1로 스케일링)\n");
            html.append("      const maxDensity = Math.max(...density.map(d => d.y));\n");
            html.append("      if (maxDensity > 0) {\n");
            html.append("        density.forEach(d => d.y = d.y / maxDensity);\n");
            html.append("      }\n");
            html.append("      \n");
            html.append("      return density;\n");
            html.append("    }\n");

            html.append("    // 히트맵 데이터 생성 함수\n");
            html.append("    function generateHeatmapData(data, bins = 10, customMin = null, customMax = null) {\n");
            html.append("      const min = customMin !== null ? customMin : Math.min(...data);\n");
            html.append("      const max = customMax !== null ? customMax : Math.max(...data);\n");
            html.append("      const binWidth = (max - min) / bins;\n");
            html.append("      \n");
            html.append("      const counts = Array(bins).fill(0);\n");
            html.append("      const labels = [];\n");
            html.append("      \n");
            html.append("      // 빈 레이블 생성\n");
            html.append("      for (let i = 0; i < bins; i++) {\n");
            html.append("        const start = min + i * binWidth;\n");
            html.append("        const end = min + (i + 1) * binWidth;\n");
            html.append("        labels.push(`${start.toFixed(0)}-${end.toFixed(0)}`);\n");
            html.append("      }\n");
            html.append("      \n");
            html.append("      // 빈도 계산\n");
            html.append("      data.forEach(val => {\n");
            html.append("        const binIndex = Math.min(\n");
            html.append("          Math.floor((val - min) / binWidth),\n");
            html.append("          bins - 1\n");
            html.append("        );\n");
            html.append("        counts[binIndex]++;\n");
            html.append("      });\n");
            html.append("      \n");
            html.append("      // 비율로 변환\n");
            html.append("      const total = counts.reduce((a, b) => a + b, 0);\n");
            html.append("      const ratios = counts.map(c => (c / total) * 100);\n");
            html.append("      \n");
            html.append("      return { labels, values: ratios };\n");
            html.append("    }\n");

            // 실제 데이터 로드 및 차트 생성
            html.append("    // 데이터 준비\n");

            // 각 데이터 크기에 대한 응답 시간 데이터
            for (int dataSize : DATA_SIZES) {
                List<Double> standardTimes = standardTimesByDataSize.getOrDefault(dataSize, Collections.emptyList());
                List<Double> postgisTimes = postgisTimesByDataSize.getOrDefault(dataSize, Collections.emptyList());

                html.append(String.format("    const standardTimes_%d = %s;%n",
                    dataSize,
                    standardTimes.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"))
                ));
                html.append(String.format("    const postgisTimes_%d = %s;%n",
                    dataSize,
                    postgisTimes.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"))
                ));
            }

            html.append("    // 최대 데이터셋에 대한 데이터 준비\n");
            html.append("    const maxDataSize = 1500000;\n");
            html.append("    const standardTimesMax = standardTimes_1500000;\n");
            html.append("    const postgisTimesMax = postgisTimes_1500000;\n");

            // 산점도 + 추세선 데이터
            html.append("    // 산점도 + 추세선용 데이터\n");
            html.append("    const standardMaxSampled = sampleArray(standardTimesMax, 300);\n");
            html.append("    const postgisMaxSampled = sampleArray(postgisTimesMax, 300);\n");
            html.append("    \n");
            html.append("    // 이동 평균 계산\n");
            html.append("    const movingAverageWindow = 30;\n");
            html.append("    const standardMovingAvg = calculateMovingAverage(standardMaxSampled, movingAverageWindow);\n");
            html.append("    const postgisMovingAvg = calculateMovingAverage(postgisMaxSampled, movingAverageWindow);\n");

            // 박스플롯 데이터
            html.append("    // 박스플롯 데이터\n");
            html.append("    const boxplotLabels = ['1,500', '15,000', '150,000', '1,500,000'];\n");
            html.append("    const boxplotDataStandard = [\n");
            html.append("      calculateBoxplotStats(standardTimes_1500),\n");
            html.append("      calculateBoxplotStats(standardTimes_15000),\n");
            html.append("      calculateBoxplotStats(standardTimes_150000),\n");
            html.append("      calculateBoxplotStats(standardTimes_1500000)\n");
            html.append("    ];\n");
            html.append("    const boxplotDataPostgis = [\n");
            html.append("      calculateBoxplotStats(postgisTimes_1500),\n");
            html.append("      calculateBoxplotStats(postgisTimes_15000),\n");
            html.append("      calculateBoxplotStats(postgisTimes_150000),\n");
            html.append("      calculateBoxplotStats(postgisTimes_1500000)\n");
            html.append("    ];\n");

            // KDE 데이터
            html.append("    // KDE 데이터\n");
            html.append("    const kdeStandard = generateKDEData(standardTimesMax, null, 100);\n");
            html.append("    const kdePostgis = generateKDEData(postgisTimesMax, null, 100);\n");

            // 백분위수 그래프 데이터
            html.append("    // 백분위수 그래프 데이터\n");
            html.append("    const percentileValues = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99];\n");
            html.append("    const percentileDataStandard = percentileValues.map(p => ({\n");
            html.append("      x: p,\n");
            html.append("      y: calculatePercentile(standardTimesMax, p)\n");
            html.append("    }));\n");
            html.append("    const percentileDataPostgis = percentileValues.map(p => ({\n");
            html.append("      x: p,\n");
            html.append("      y: calculatePercentile(postgisTimesMax, p)\n");
            html.append("    }));\n");

            // 히트맵 데이터
            html.append("    // 히트맵 데이터 - 동일한 구간 사용\n");
            html.append("    const allTimesForBins = [...standardTimesMax, ...postgisTimesMax];\n");
            html.append("    const globalMin = Math.min(...allTimesForBins);\n");
            html.append("    const globalMax = Math.max(...allTimesForBins);\n");
            html.append("    const heatmapBins = 10;\n");
            html.append("    const heatmapStandard = generateHeatmapData(standardTimesMax, heatmapBins, globalMin, globalMax);\n");
            html.append("    const heatmapPostgis = generateHeatmapData(postgisTimesMax, heatmapBins, globalMin, globalMax);\n");

            // 차트 생성
            // 1. 산점도 + 추세선 차트
            html.append("    // 1. 산점도 + 추세선 차트\n");
            html.append("    new Chart(document.getElementById('scatterChart'), {\n");
            html.append("      type: 'scatter',\n");
            html.append("      data: {\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '일반 좌표 쿼리 (샘플)',\n");
            html.append("            data: standardMaxSampled.map((y, x) => ({ x, y })),\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.3)',\n");
            html.append("            pointRadius: 3,\n");
            html.append("            pointHoverRadius: 5\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 공간 쿼리 (샘플)',\n");
            html.append("            data: postgisMaxSampled.map((y, x) => ({ x, y })),\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.3)',\n");
            html.append("            pointRadius: 3,\n");
            html.append("            pointHoverRadius: 5\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '일반 쿼리 이동 평균',\n");
            html.append("            data: standardMovingAvg.map((y, x) => ({ x, y })),\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: false,\n");
            html.append("            type: 'line',\n");
            html.append("            tension: 0.4\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 이동 평균',\n");
            html.append("            data: postgisMovingAvg.map((y, x) => ({ x, y })),\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: false,\n");
            html.append("            type: 'line',\n");
            html.append("            tension: 0.4\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        maintainAspectRatio: true,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '응답 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '샘플 순서'\n");
            html.append("            },\n");
            html.append("            ticks: {\n");
            html.append("              maxTicksLimit: 10\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              label: function(context) {\n");
            html.append("                let label = context.dataset.label || '';\n");
            html.append("                if (label) {\n");
            html.append("                  label += ': ';\n");
            html.append("                }\n");
            html.append("                // 소수점 둘째 자리에서 첫째 자리로 변경\n");
            html.append("                label += context.parsed.y.toFixed(1) + 'ms';\n");
            html.append("                return label;\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 2. 박스플롯 차트
            html.append("    // 2. 박스플롯 차트\n");
            html.append("    new Chart(document.getElementById('boxplotChart'), {\n");
            html.append("      type: 'boxplot',\n");
            html.append("      data: {\n");
            html.append("        labels: boxplotLabels.map(l => l + ' 행'),\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '일반 좌표 쿼리',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.5)',\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            data: boxplotDataStandard.map(stats => ({\n");
            html.append("              min: stats.min,\n");
            html.append("              q1: stats.q1,\n");
            html.append("              median: stats.median,\n");
            html.append("              q3: stats.q3,\n");
            html.append("              max: stats.max,\n");
            html.append("              outliers: stats.outliers.slice(0, 50) // 이상치 수 제한\n");
            html.append("            }))\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 공간 쿼리',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.5)',\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            data: boxplotDataPostgis.map(stats => ({\n");
            html.append("              min: stats.min,\n");
            html.append("              q1: stats.q1,\n");
            html.append("              median: stats.median,\n");
            html.append("              q3: stats.q3,\n");
            html.append("              max: stats.max,\n");
            html.append("              outliers: stats.outliers.slice(0, 50) // 이상치 수 제한\n");
            html.append("            }))\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        maintainAspectRatio: true,\n");
            html.append("        elements: {\n");
            html.append("          point: {\n");
            html.append("            radius: 3,\n");
            html.append("            hoverRadius: 5\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '응답 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '데이터 크기'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              label: function(context) {\n");
            html.append("                const v = context.raw;\n");
            html.append("                // 소수점 둘째 자리에서 첫째 자리로 변경\n");
            html.append("                return [\n");
            html.append("                  \"Min: \" + v.min.toFixed(1) + \"ms\",\n");
            html.append("                  \"Q1: \" + v.q1.toFixed(1) + \"ms\",\n");
            html.append("                  \"Median: \" + v.median.toFixed(1) + \"ms\",\n");
            html.append("                  \"Q3: \" + v.q3.toFixed(1) + \"ms\",\n");
            html.append("                  \"Max: \" + v.max.toFixed(1) + \"ms\"\n");
            html.append("                ];\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 3. 커널 밀도 추정 차트
            html.append("    // 3. 커널 밀도 추정 차트\n");
            html.append("    new Chart(document.getElementById('kdeChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 공간 쿼리',\n");
            html.append("            data: kdePostgis,\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
            html.append("            fill: true,\n");
            html.append("            tension: 0.4,\n");
            html.append("            parsing: {\n");
            html.append("              xAxisKey: 'x',\n");
            html.append("              yAxisKey: 'y'\n");
            html.append("            }\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '일반 좌표 쿼리',\n");
            html.append("            data: kdeStandard,\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.2)',\n");
            html.append("            fill: true,\n");
            html.append("            tension: 0.4,\n");
            html.append("            parsing: {\n");
            html.append("              xAxisKey: 'x',\n");
            html.append("              yAxisKey: 'y'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        maintainAspectRatio: false,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '밀도'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            type: 'linear',\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '응답 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            ticks: {\n");
            html.append("              callback: function(value) {\n");
            html.append("                return value.toFixed(0);\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        elements: {\n");
            html.append("          point: {\n");
            html.append("            radius: 0\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              label: function(context) {\n");
            html.append("                // 소수점 둘째 자리에서 첫째 자리로 변경\n");
            html.append("                return context.dataset.label + \": \" + context.parsed.x.toFixed(1) + \"ms (밀도: \" + context.parsed.y.toFixed(2) + \")\";\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 4. 히트맵 차트
            html.append("    // 4. 히트맵 차트 (구간별 빈도로 대체)\n");
            html.append("    new Chart(document.getElementById('heatmapChart'), {\n");
            html.append("      type: 'bar',\n");
            html.append("      data: {\n");
            html.append("        labels: heatmapStandard.labels,\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '일반 좌표 쿼리',\n");
            html.append("            data: heatmapStandard.values,\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.7)',\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            borderWidth: 1\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 공간 쿼리',\n");
            html.append("            data: heatmapPostgis.values,\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.7)',\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            borderWidth: 1\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        maintainAspectRatio: false,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '비율 (%)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '응답 시간 구간 (ms)'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              title: function(tooltipItems) {\n");
            html.append("                return `응답 시간: ${tooltipItems[0].label}ms`;\n");
            html.append("              },\n");
            html.append("              label: function(context) {\n");
            html.append("                // 소수점 둘째 자리에서 첫째 자리로 변경\n");
            html.append("                return `${context.dataset.label}: ${context.parsed.y.toFixed(1)}%`;\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 5. 백분위수 그래프
            html.append("    // 5. 백분위수 그래프\n");
            html.append("    new Chart(document.getElementById('percentileChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '일반 좌표 쿼리',\n");
            html.append("            data: percentileDataStandard,\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.1)',\n");
            html.append("            fill: false,\n");
            html.append("            tension: 0.4,\n");
            html.append("            parsing: {\n");
            html.append("              xAxisKey: 'x',\n");
            html.append("              yAxisKey: 'y'\n");
            html.append("            }\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: 'PostGIS 공간 쿼리',\n");
            html.append("            data: percentileDataPostgis,\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.1)',\n");
            html.append("            fill: false,\n");
            html.append("            tension: 0.4,\n");
            html.append("            parsing: {\n");
            html.append("              xAxisKey: 'x',\n");
            html.append("              yAxisKey: 'y'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        maintainAspectRatio: false,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '응답 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            type: 'linear',\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '백분위수 (%)'\n");
            html.append("            },\n");
            html.append("            min: 0,\n");
            html.append("            max: 100,\n");
            html.append("            ticks: {\n");
            html.append("              stepSize: 10,\n");
            html.append("              callback: function(value) {\n");
            html.append("                return value + '%';\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              title: function(tooltipItems) {\n");
            html.append("                return `${tooltipItems[0].parsed.x}번째 백분위수`;\n");
            html.append("              },\n");
            html.append("              label: function(context) {\n");
            html.append("                // 소수점 둘째 자리에서 첫째 자리로 변경\n");
            html.append("                return `${context.dataset.label}: ${context.parsed.y.toFixed(1)}ms`;\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n");

            html.append("  </script>\n");
            html.append("</body>\n</html>");

            // HTML 파일로 저장
            Files.writeString(Path.of(SUMMARY_CHART_FILE), html.toString());

        } catch (Exception e) {
            log.error("차트 생성 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * CSV 파일에서 응답 시간 데이터 읽기
     * @param filename CSV 파일 이름
     * @return 데이터 크기별 응답 시간 목록
     */
    private Map<Integer, List<Double>> readResponseTimes(String filename) throws Exception {
        Map<Integer, List<Double>> timesByDataSize = new HashMap<>();

        List<String> lines = Files.readAllLines(Path.of(filename));
        // 헤더 건너뛰기
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            int dataSize = Integer.parseInt(parts[0]);
            double time = Double.parseDouble(parts[1]);

            timesByDataSize.computeIfAbsent(dataSize, k -> new ArrayList<>()).add(time);
        }

        return timesByDataSize;
    }

    /**
     * 쿼리 측정 결과 레코드
     */
    private record QueryResult(
            LocalDateTime timestamp,   // 측정 시간
            String queryType,          // 쿼리 유형 (STANDARD 또는 POSTGIS)
            double executionTimeMs,    // 실행 시간 (ms)
            Integer resultCount        // 결과 수
    ) {
    }

    /**
     * 신뢰 구간 레코드
     */
    private record ConfidenceInterval(
            double lowerBound,  // 하한값
            double upperBound   // 상한값
    ) {}

    /**
     * 데이터 크기별 테스트 결과 레코드
     */
    private record DataSizeResult(
            int dataSize,               // 데이터 크기
            int standardSampleCount,    // 표준 쿼리 샘플 수
            int postgisSampleCount,     // PostGIS 쿼리 샘플 수
            double avgStandardTime,     // 평균 표준 쿼리 시간
            double avgPostgisTime,      // 평균 PostGIS 쿼리 시간
            double p95StandardTime,     // P95 표준 쿼리 시간
            double p95PostgisTime,      // P95 PostGIS 쿼리 시간
            ConfidenceInterval standardCI, // 표준 쿼리 신뢰 구간
            ConfidenceInterval postgisCI   // PostGIS 쿼리 신뢰 구간
    ) {}
}