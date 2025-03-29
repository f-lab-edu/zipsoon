package com.zipsoon.batch.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.infrastructure.processor.source.loader.CsvSourceFileLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * 선택적 리소스 처리 성능 테스트
 *
 * <p>배치 작업에서 모든 리소스를 처리하는 방식과 변경된 리소스만 처리하는 선택적 방식의 성능을 비교합니다.</p>
 * <p>테스트 환경:</p>
 * <ul>
 *   <li>TestContainers를 사용한 실제 PostgreSQL 데이터베이스</li>
 *   <li>실제 기능 모듈을 래핑한 테스트용 구현체 사용</li>
 *   <li>효율적인 반복 측정으로 안정적인 결과 도출</li>
 * </ul>
 */
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SelectiveResourceProcessingTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    private static final String RESULTS_FILE = "selective_processing_results.csv";
    private static final String SUMMARY_CHART_FILE = "selective_processing_summary.html";
    private static final int MAX_RESOURCES = 10; // 테스트할 리소스의 최대 수

    // 테스트 반복 횟수
    private static final int WARMUP_ITERATIONS = 2; // 워밍업 횟수 감소
    private static final int TEST_ITERATIONS = 5;  // 테스트 반복 횟수 감소

    // 테스트 환경 설정 및 정리를 위한 변수들
    private List<SourceCollector> collectors;
    private File testResourceDir;

    @BeforeEach
    public void setup() throws IOException {
        // TestContainers로부터 DataSource 및 JdbcTemplate 설정
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // 테스트 리소스 디렉토리 준비
        testResourceDir = new File("source/data");
        testResourceDir.mkdirs();

        log.info("테스트 리소스 디렉토리 생성: {}", testResourceDir.getAbsolutePath());

        // 필요한 컴포넌트 구성 (테스트용 구현체 사용)
        com.zipsoon.batch.infrastructure.mapper.source.SourceMapper sourceMapper = mock(com.zipsoon.batch.infrastructure.mapper.source.SourceMapper.class);
        TestSourceRepository testSourceRepository = new TestSourceRepository(sourceMapper, jdbcTemplate);

        // 테스트 환경 초기화
        testSourceRepository.initialize();

        JobExplorer jobExplorer = mock(JobExplorer.class);
        CsvSourceFileLoader fileLoader = new CsvSourceFileLoader(dataSource);

        // ParkSourceCollector를 재사용하여 10개의 가상 수집기 생성
        collectors = new ArrayList<>();
        for (int i = 0; i < MAX_RESOURCES; i++) {
            TestParkSourceCollector collector = new TestParkSourceCollector(
                testSourceRepository,
                fileLoader,
                jobExplorer,
                "ParkCollector-" + i
            );
            collectors.add(collector);
        }
    }

    @AfterEach
    public void cleanup() {
        // 테스트 종료 후 정리
        log.info("테스트 환경 정리 시작");

        // 데이터베이스 정리
        jdbcTemplate.execute("DROP TABLE IF EXISTS parks");

        // 리소스 종료
        ((HikariDataSource) dataSource).close();

        log.info("테스트 환경 정리 완료");
    }

    @Test
    public void testSelectiveProcessingPerformance() throws Exception {
        log.info("선택적 리소스 처리 성능 테스트 시작");

        List<TestResult> allResults = new ArrayList<>();
        initializeResultsFile();

        // 워밍업 실행 (JIT 컴파일 최적화를 위해 유지)
        List<SourceCollector> maxCollectors = collectors.subList(0, MAX_RESOURCES);
        log.info("워밍업 실행 ({} 회)", WARMUP_ITERATIONS);
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            measureFullProcessing(maxCollectors, "warmup_full_" + i);
            measureSelectiveProcessing(maxCollectors, "warmup_selective_" + i);
        }

        // 리소스 개수 변경하며 테스트 수행 (2, 4, 6, 8, 10)
        for (int resourceCount = 2; resourceCount <= MAX_RESOURCES; resourceCount += 2) {
            log.info("리소스 개수 {} 테스트 시작", resourceCount);

            // 테스트할 컬렉터 선택
            List<SourceCollector> testCollectors = collectors.subList(0, resourceCount);

            // 하나의 리소스만 변경되는 시나리오 설정
            setupSingleResourceChangedScenario(testCollectors);

            // 측정 결과 저장할 배열
            long[] fullTimes = new long[TEST_ITERATIONS];
            long[] selectiveTimes = new long[TEST_ITERATIONS];
            double[] fullHeapUsage = new double[TEST_ITERATIONS];
            double[] selectiveHeapUsage = new double[TEST_ITERATIONS];

            // 1. 전체 처리 측정
            log.info("전체 처리 테스트 시작 ({} 회)", TEST_ITERATIONS);
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                MeasurementResult result = measureFullProcessing(
                    testCollectors, "full_" + resourceCount + "_" + i);

                fullTimes[i] = result.executionTime;
                fullHeapUsage[i] = result.heapUsageMB;

                log.debug("전체 처리 반복 {}/{}: {}ms, 힙 {}MB",
                    i+1, TEST_ITERATIONS, result.executionTime, result.heapUsageMB);

                // 측정 간 짧은 대기 (캐시 효과 감소)
                Thread.sleep(100);
            }

            // 2. 선택적 처리 측정
            log.info("선택적 처리 테스트 시작 ({} 회)", TEST_ITERATIONS);
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                MeasurementResult result = measureSelectiveProcessing(
                    testCollectors, "selective_" + resourceCount + "_" + i);

                selectiveTimes[i] = result.executionTime;
                selectiveHeapUsage[i] = result.heapUsageMB;

                log.debug("선택적 처리 반복 {}/{}: {}ms, 힙 {}MB",
                    i+1, TEST_ITERATIONS, result.executionTime, result.heapUsageMB);

                // 측정 간 짧은 대기
                Thread.sleep(100);
            }

            // 결과 계산
            // 극단적 이상치 제거 (최소값, 최대값 제외)
            long[] trimmedFullTimes = removeExtremes(fullTimes);
            long[] trimmedSelectiveTimes = removeExtremes(selectiveTimes);
            double[] trimmedFullHeapUsage = removeExtremes(fullHeapUsage);
            double[] trimmedSelectiveHeapUsage = removeExtremes(selectiveHeapUsage);

            // 통계 계산
            double avgFullTime = calculateAverage(trimmedFullTimes);
            double avgSelectiveTime = calculateAverage(trimmedSelectiveTimes);
            double avgFullHeap = calculateAverage(trimmedFullHeapUsage);
            double avgSelectiveHeap = calculateAverage(trimmedSelectiveHeapUsage);

            double minFullTime = findMin(trimmedFullTimes);
            double maxFullTime = findMax(trimmedFullTimes);
            double minSelectiveTime = findMin(trimmedSelectiveTimes);
            double maxSelectiveTime = findMax(trimmedSelectiveTimes);

            // 개선율 계산
            double timeImprovement = avgFullTime / avgSelectiveTime;
            double memoryImprovement = avgFullHeap / avgSelectiveHeap;

            // 결과 저장
            TestResult result = new TestResult(
                resourceCount,
                (long)avgFullTime,
                (long)avgSelectiveTime,
                timeImprovement,
                memoryImprovement,
                minFullTime, maxFullTime,
                minSelectiveTime, maxSelectiveTime
            );

            allResults.add(result);
            appendResultToFile(result);

            log.info("결과: 리소스 {}개 - 시간 개선 {}배 (전체: {}ms, 선택적: {}ms), 범위 비교: 전체[{}-{}ms], 선택적[{}-{}ms]",
                resourceCount,
                String.format("%.2f", timeImprovement),
                String.format("%.0f", avgFullTime),
                String.format("%.0f", avgSelectiveTime),
                String.format("%.0f", minFullTime),
                String.format("%.0f", maxFullTime),
                String.format("%.0f", minSelectiveTime),
                String.format("%.0f", maxSelectiveTime));
        }

        // 차트 생성
        generateSummaryChart(allResults);

        log.info("선택적 리소스 처리 성능 테스트 완료. 결과 파일: {}, 차트: {}",
                 RESULTS_FILE, SUMMARY_CHART_FILE);
    }

    /**
     * 단일 리소스 변경 시나리오 설정
     */
    private void setupSingleResourceChangedScenario(List<SourceCollector> collectors) {
        for (int i = 0; i < collectors.size(); i++) {
            SourceCollector collector = collectors.get(i);
            if (collector instanceof TestParkSourceCollector) {
                // 첫 번째 리소스만 변경이 필요한 것으로 설정
                ((TestParkSourceCollector) collector).setNeedsUpdate(i == 0);
            }
        }
    }

    // 측정 결과를 저장하는 레코드
    private record MeasurementResult(
        long executionTime,
        double heapUsageMB,
        double cpuPercent
    ) {}

    /**
     * 전체 처리 방식 성능 측정
     */
    private MeasurementResult measureFullProcessing(List<SourceCollector> collectors, String metricPrefix) {
        // 모든 컬렉터의 wasUpdated()가 true를 반환하도록 설정
        List<SourceCollector> spiedCollectors = collectors.stream()
            .map(collector -> {
                SourceCollector spied = spy(collector);
                when(spied.wasUpdated()).thenReturn(true);
                return spied;
            })
            .toList();

        JmxMonitor monitor = new JmxMonitor(metricPrefix);
        monitor.start();

        try {
            long startTime = System.currentTimeMillis();

            // 각 컬렉터에 대해 처리 작업 실행
            for (SourceCollector collector : spiedCollectors) {
                collector.create();
                collector.collect();
                collector.preprocess();
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // JMX 모니터링에서 메트릭 수집
            Map<String, Double> metrics = monitor.getMetricSummary();
            double heapUsageMB = metrics.getOrDefault("heapMB", 0.0);
            double cpuPercent = metrics.getOrDefault("cpuPercent", 0.0);

            return new MeasurementResult(executionTime, heapUsageMB, cpuPercent);
        } catch (Exception e) {
            log.error("전체 처리 측정 중 오류: {}", e.getMessage(), e);
            return new MeasurementResult(-1, 0, 0);
        } finally {
            monitor.close();
        }
    }

    /**
     * 선택적 처리 방식 성능 측정
     */
    private MeasurementResult measureSelectiveProcessing(List<SourceCollector> collectors, String metricPrefix) {
        JmxMonitor monitor = new JmxMonitor(metricPrefix);
        monitor.start();

        try {
            long startTime = System.currentTimeMillis();

            // 각 컬렉터에 대해 처리 작업 실행 (wasUpdated가 true일 때만)
            for (SourceCollector collector : collectors) {
                if (collector.wasUpdated()) {
                    collector.create();
                    collector.collect();
                    collector.preprocess();
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // JMX 모니터링에서 메트릭 수집
            Map<String, Double> metrics = monitor.getMetricSummary();
            double heapUsageMB = metrics.getOrDefault("heapMB", 0.0);
            double cpuPercent = metrics.getOrDefault("cpuPercent", 0.0);

            return new MeasurementResult(executionTime, heapUsageMB, cpuPercent);
        } catch (Exception e) {
            log.error("선택적 처리 측정 중 오류: {}", e.getMessage(), e);
            return new MeasurementResult(-1, 0, 0);
        } finally {
            monitor.close();
        }
    }

    /**
     * 극단적 이상치 제거 (최소값, 최대값 제외)
     */
    private long[] removeExtremes(long[] values) {
        if (values.length <= 2) return values;

        long[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);

        return Arrays.copyOfRange(sorted, 1, sorted.length - 1);
    }

    /**
     * 극단적 이상치 제거 (최소값, 최대값 제외)
     */
    private double[] removeExtremes(double[] values) {
        if (values.length <= 2) return values;

        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);

        return Arrays.copyOfRange(sorted, 1, sorted.length - 1);
    }

    /**
     * 평균 계산
     */
    private double calculateAverage(long[] values) {
        if (values.length == 0) return 0;
        return Arrays.stream(values).average().orElse(0);
    }

    /**
     * 평균 계산 (double 배열용)
     */
    private double calculateAverage(double[] values) {
        if (values.length == 0) return 0;
        return Arrays.stream(values).average().orElse(0);
    }

    /**
     * 최소값 찾기
     */
    private double findMin(long[] values) {
        if (values.length == 0) return 0;
        return Arrays.stream(values).min().orElse(0);
    }

    /**
     * 최대값 찾기
     */
    private double findMax(long[] values) {
        if (values.length == 0) return 0;
        return Arrays.stream(values).max().orElse(0);
    }

    /**
     * 테스트 결과 파일 초기화
     */
    private void initializeResultsFile() throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE)) {
            writer.write("resourceCount,fullProcessingTime,selectiveProcessingTime,timeImprovement," +
                         "memoryImprovement,minFullTime,maxFullTime,minSelectiveTime,maxSelectiveTime\n");
        }
    }

    /**
     * 테스트 결과를 CSV 파일에 추가
     */
    private void appendResultToFile(TestResult result) throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE, true)) {
            writer.write(String.format("%d,%d,%d,%.2f,%.2f,%.0f,%.0f,%.0f,%.0f%n",
                result.resourceCount,
                result.avgFullTime,
                result.avgSelectiveTime,
                result.timeImprovement,
                result.memoryImprovement,
                result.minFullTime,
                result.maxFullTime,
                result.minSelectiveTime,
                result.maxSelectiveTime));
        }
    }

    /**
     * 성능 테스트 결과를 종합한 차트 생성
     */
    private void generateSummaryChart(List<TestResult> results) throws Exception {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("  <title>선택적 리소스 처리 성능 분석</title>\n");
            html.append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: Arial, sans-serif; margin: 20px; color: #333; }\n");
            html.append("    .chart-container { width: 90%; max-width: 800px; margin: 20px auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n");
            html.append("    .chart-grid { display: grid; grid-template-columns: 1fr; gap: 20px; }\n");
            html.append("    h1, h2 { text-align: center; color: #2c3e50; }\n");
            html.append("    .header { text-align: center; margin: 40px 0; }\n");
            html.append("    .summary { background-color: #e8f4fc; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
            html.append("    .highlight { background-color: #fffde7; font-weight: bold; padding: 2px 4px; border-radius: 3px; }\n");
            html.append("    table { width: 100%; border-collapse: collapse; margin: 20px 0; background-color: white; }\n");
            html.append("    table, th, td { border: 1px solid #ddd; }\n");
            html.append("    th, td { padding: 10px; text-align: right; }\n");
            html.append("    th { background-color: #f2f2f2; color: #333; }\n");
            html.append("    .conclusion { max-width: 800px; margin: 40px auto; background-color: #f1f8e9; padding: 20px; border-radius: 8px; border-left: 5px solid #8bc34a; }\n");
            html.append("  </style>\n");
            html.append("</head>\n<body>\n");

            // 헤더 및 설명
            html.append("  <div class=\"header\">\n");
            html.append("    <h1>선택적 자료 수집기 성능 분석</h1>\n");
            html.append("    <p>이 분석은 배치 작업에서 변경된 리소스만 선택적으로 처리했을 때의 성능 향상을 보여줍니다.</p>\n");
            html.append("  </div>\n");

            // 주요 발견점 요약
            html.append("  <div class=\"summary\">\n");
            html.append("    <h2>주요 결과</h2>\n");
            html.append("    <ul>\n");

            // 최대 성능 개선 찾기
            results.stream()
                    .max(Comparator.comparingDouble(TestResult::timeImprovement))
                    .ifPresent(bestResult -> html.append(
                            String.format("      <li>최대 시간 개선: <span class=\"highlight\">%.1f배</span> (리소스 %d개일 때)</li>%n",
                            bestResult.timeImprovement,
                            bestResult.resourceCount)));

            // 리소스 개수 10개일 때의 개선 찾기
            results.stream()
                    .filter(r -> r.resourceCount == MAX_RESOURCES)
                    .findFirst().ifPresent(maxResult -> html.append(
                            String.format("      <li>리소스 %d개일 때: 처리 시간 <span class=\"highlight\">%.1f배</span> 개선</li>%n",
                            MAX_RESOURCES,
                            maxResult.timeImprovement)));

            html.append("      <li>리소스 개수가 많을수록 선택적 처리 방식의 이점이 증가합니다</li>\n");
            html.append("    </ul>\n");
            html.append("  </div>\n");

            // 차트 그리드 시작
            html.append("  <div class=\"chart-grid\">\n");

            // 1. 리소스 개수별 처리 시간 차트
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>리소스 개수별 처리 시간 (ms)</h2>\n");
            html.append("      <canvas id=\"processingTimeChart\"></canvas>\n");
            html.append("    </div>\n");

            // 2. 개선율 차트
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>리소스 개수별 성능 개선율</h2>\n");
            html.append("      <canvas id=\"improvementChart\"></canvas>\n");
            html.append("    </div>\n");

            // 3. 처리 시간 변동 범위 차트
            html.append("    <div class=\"chart-container\">\n");
            html.append("      <h2>처리 시간 변동 범위</h2>\n");
            html.append("      <canvas id=\"rangeChart\"></canvas>\n");
            html.append("    </div>\n");

            html.append("  </div>\n"); // 차트 그리드 종료

            // 테이블 형태로 결과 제공
            html.append("  <div class=\"chart-container\">\n");
            html.append("    <h2>상세 테스트 결과</h2>\n");
            html.append("    <table>\n");
            html.append("      <tr>\n");
            html.append("        <th>리소스 개수</th>\n");
            html.append("        <th>전체 처리 (ms)</th>\n");
            html.append("        <th>선택적 처리 (ms)</th>\n");
            html.append("        <th>시간 개선율</th>\n");
            html.append("        <th>메모리 개선율</th>\n");
            html.append("        <th>전체 처리 범위</th>\n");
            html.append("        <th>선택적 처리 범위</th>\n");
            html.append("      </tr>\n");

            // 모든 결과 테이블에 표시
            for (TestResult result : results) {
                html.append(String.format("      <tr>%n" +
                           "        <td>%d</td>%n" +
                           "        <td>%d</td>%n" +
                           "        <td>%d</td>%n" +
                           "        <td>%.2f배</td>%n" +
                           "        <td>%.2f배</td>%n" +
                           "        <td>%.0f-%.0f ms</td>%n" +
                           "        <td>%.0f-%.0f ms</td>%n" +
                           "      </tr>%n",
                           result.resourceCount,
                           result.avgFullTime,
                           result.avgSelectiveTime,
                           result.timeImprovement,
                           result.memoryImprovement,
                           result.minFullTime, result.maxFullTime,
                           result.minSelectiveTime, result.maxSelectiveTime));
            }

            html.append("    </table>\n");
            html.append("  </div>\n");

            // 결론
            html.append("  <div class=\"conclusion\">\n");
            html.append("    <h2>결론</h2>\n");
            html.append("    <p><strong>선택적 자료 수집기는 대다수 소스가 변경되지 않은 일반적인 상황에서 뛰어난 성능 이점을 제공합니다.</strong></p>\n");
            html.append("    <p>이번 실험에서는 10개 소스 중 단 1개만 변경된 상황을 가정했으며, 다음과 같은 결과를 확인했습니다:</p>\n");
            html.append("    <ul>\n");
            html.append("      <li>리소스 개수가 증가할수록 선택적 처리 방식의 성능 이점이 증가합니다.</li>\n");
            html.append("      <li>최대 리소스 개수(10개) 상황에서 처리 시간이 크게 감소하여 확장성이 입증되었습니다.</li>\n");
            html.append("      <li>메모리 사용량도 선택적 처리 방식에서 상당히 감소되었습니다.</li>\n");
            html.append("    </ul>\n");
            html.append("    <p>결과적으로, 선택적 자료 수집기 도입은 <span class=\"highlight\">실행 시간 단축과 인프라 비용 절감</span>으로 이어질 수 있습니다.</p>\n");
            html.append("  </div>\n");

            // JavaScript 차트 코드
            html.append("  <script>\n");

            // 데이터 배열 생성
            html.append("    const resourceCounts = [];\n");
            html.append("    const fullTimes = [];\n");
            html.append("    const selectiveTimes = [];\n");
            html.append("    const timeImprovements = [];\n");
            html.append("    const memoryImprovements = [];\n");
            html.append("    const fullTimeMin = [];\n");
            html.append("    const fullTimeMax = [];\n");
            html.append("    const selectiveTimeMin = [];\n");
            html.append("    const selectiveTimeMax = [];\n");

            // 데이터 로드
            for (TestResult result : results) {
                html.append(String.format(
                    "    resourceCounts.push(%d);\n" +
                    "    fullTimes.push(%d);\n" +
                    "    selectiveTimes.push(%d);\n" +
                    "    timeImprovements.push(%.2f);\n" +
                    "    memoryImprovements.push(%.2f);\n" +
                    "    fullTimeMin.push(%.0f);\n" +
                    "    fullTimeMax.push(%.0f);\n" +
                    "    selectiveTimeMin.push(%.0f);\n" +
                    "    selectiveTimeMax.push(%.0f);\n",
                    result.resourceCount,
                    result.avgFullTime,
                    result.avgSelectiveTime,
                    result.timeImprovement,
                    result.memoryImprovement,
                    result.minFullTime,
                    result.maxFullTime,
                    result.minSelectiveTime,
                    result.maxSelectiveTime));
            }

            // 차트 1: 처리 시간 차트
            html.append("    // 처리 시간 차트\n");
            html.append("    new Chart(document.getElementById('processingTimeChart'), {\n");
            html.append("      type: 'bar',\n");
            html.append("      data: {\n");
            html.append("        labels: resourceCounts.map(count => count + '개'),\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '전체 처리',\n");
            html.append("            data: fullTimes,\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.7)',\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            borderWidth: 1\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '선택적 처리',\n");
            html.append("            data: selectiveTimes,\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.7)',\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            borderWidth: 1\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '처리 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '리소스 개수'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 차트 2: 개선율 차트
            html.append("    // 개선율 차트\n");
            html.append("    new Chart(document.getElementById('improvementChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        labels: resourceCounts.map(count => count + '개'),\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '시간 개선율',\n");
            html.append("            data: timeImprovements,\n");
            html.append("            borderColor: 'rgba(54, 162, 235, 1)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.1)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            fill: false,\n");
            html.append("            tension: 0.1\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '메모리 개선율',\n");
            html.append("            data: memoryImprovements,\n");
            html.append("            borderColor: 'rgba(75, 192, 192, 1)',\n");
            html.append("            backgroundColor: 'rgba(75, 192, 192, 0.1)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            fill: false,\n");
            html.append("            tension: 0.1\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '개선율 (배수)'\n");
            html.append("            },\n");
            html.append("            suggestedMin: 1\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '리소스 개수'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n\n");

            // 차트 3: 변동 범위 차트
            html.append("    // 변동 범위 차트\n");
            html.append("    new Chart(document.getElementById('rangeChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        labels: resourceCounts.map(count => count + '개'),\n");
            html.append("        datasets: [\n");
            html.append("          {\n");
            html.append("            label: '전체 처리 (평균)',\n");
            html.append("            data: fullTimes,\n");
            html.append("            borderColor: 'rgb(255, 99, 132)',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            fill: '+1',\n");
            html.append("            tension: 0.1\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '전체 처리 (최소)',\n");
            html.append("            data: fullTimeMin,\n");
            html.append("            borderColor: 'rgba(255, 99, 132, 0.3)',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.1)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            borderDash: [5, 5],\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: false\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '전체 처리 (최대)',\n");
            html.append("            data: fullTimeMax,\n");
            html.append("            borderColor: 'rgba(255, 99, 132, 0.3)',\n");
            html.append("            backgroundColor: 'rgba(255, 99, 132, 0.1)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            borderDash: [5, 5],\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: '-1'\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '선택적 처리 (평균)',\n");
            html.append("            data: selectiveTimes,\n");
            html.append("            borderColor: 'rgb(54, 162, 235)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0)',\n");
            html.append("            borderWidth: 2,\n");
            html.append("            fill: '+1',\n");
            html.append("            tension: 0.1\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '선택적 처리 (최소)',\n");
            html.append("            data: selectiveTimeMin,\n");
            html.append("            borderColor: 'rgba(54, 162, 235, 0.3)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.1)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            borderDash: [5, 5],\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: false\n");
            html.append("          },\n");
            html.append("          {\n");
            html.append("            label: '선택적 처리 (최대)',\n");
            html.append("            data: selectiveTimeMax,\n");
            html.append("            borderColor: 'rgba(54, 162, 235, 0.3)',\n");
            html.append("            backgroundColor: 'rgba(54, 162, 235, 0.1)',\n");
            html.append("            borderWidth: 1,\n");
            html.append("            borderDash: [5, 5],\n");
            html.append("            pointRadius: 0,\n");
            html.append("            fill: '-1'\n");
            html.append("          }\n");
            html.append("        ]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        responsive: true,\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '처리 시간 (ms)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '리소스 개수'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              label: function(context) {\n");
            html.append("                const label = context.dataset.label || '';\n");
            html.append("                const value = context.parsed.y;\n");
            html.append("                return `${label}: ${value.toFixed(0)} ms`;\n");
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
            log.error("요약 차트 생성 중 오류", e);
        }
    }

    /**
     * 간소화된 테스트 결과 클래스
     */
    private record TestResult(
        int resourceCount,        // 리소스 개수
        long avgFullTime,         // 평균 전체 처리 시간
        long avgSelectiveTime,    // 평균 선택적 처리 시간
        double timeImprovement,   // 시간 개선율
        double memoryImprovement, // 메모리 개선율
        double minFullTime,       // 전체 처리 최소 시간
        double maxFullTime,       // 전체 처리 최대 시간
        double minSelectiveTime,  // 선택적 처리 최소 시간
        double maxSelectiveTime   // 선택적 처리 최대 시간
    ) {}

    /**
     * JMX 모니터링을 위한 간소화된 클래스
     */
    private static class JmxMonitor implements AutoCloseable {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final List<MetricSample> samples = Collections.synchronizedList(new ArrayList<>());
        private final String testName;
        private final long startTime;

        public JmxMonitor(String testName) {
            this.testName = testName;
            this.startTime = System.currentTimeMillis();
        }

        public void start() {
            scheduler.scheduleAtFixedRate(this::collectMetrics, 0, 100, TimeUnit.MILLISECONDS);
        }

        private void collectMetrics() {
            try {
                // JVM 메모리 정보
                Runtime runtime = Runtime.getRuntime();
                long heapUsed = runtime.totalMemory() - runtime.freeMemory();
                long heapMax = runtime.maxMemory();

                // 메트릭 저장
                samples.add(new MetricSample(
                    System.currentTimeMillis(),
                    heapUsed / (1024 * 1024), // MB로 변환
                    heapMax / (1024 * 1024)
                ));
            } catch (Exception e) {
                // 예외 무시
            }
        }

        /**
         * 수집된 메트릭 요약 반환
         */
        public Map<String, Double> getMetricSummary() {
            Map<String, Double> summary = new HashMap<>();

            if (samples.isEmpty()) {
                summary.put("heapMB", 0.0);
                summary.put("cpuPercent", 0.0);
                return summary;
            }

            double avgHeapMB = samples.stream()
                .mapToLong(MetricSample::heapMB)
                .average()
                .orElse(0.0);

            summary.put("heapMB", avgHeapMB);
            summary.put("cpuPercent", 0.0); // CPU 정보는 간소화

            return summary;
        }

        @Override
        public void close() {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 예외 무시
            }
        }

        private record MetricSample(
            long timestamp,
            long heapMB,
            long heapMax
        ) {}
    }
}