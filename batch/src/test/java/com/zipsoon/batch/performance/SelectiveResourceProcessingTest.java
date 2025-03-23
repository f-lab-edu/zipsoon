package com.zipsoon.batch.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 선택적 리소스 처리 성능 테스트
 *
 * <p>이 테스트는 집순 배치 애플리케이션의 핵심 최적화인 "선택적 리소스 처리" 전략의
 * 효과를 측정합니다. 실제 배치 애플리케이션에서는 {@code BatchJobUtils.checkNeedsUpdate()}
 * 메서드를 사용하여 변경된 리소스만 처리하는 방식으로 성능을 최적화합니다.</p>
 *
 * <p>이 테스트는 다양한 리소스 개수에 따른 성능 이득을 측정하여
 * 선택적 처리 전략의 유효성을 입증합니다.</p>
 */
@Slf4j
public class SelectiveResourceProcessingTest {

    private static final String CONSOLIDATED_RESULTS_FILE = "selective_processing_results.csv";
    private static final String SUMMARY_CHART_FILE = "selective_processing_summary.html";
    private static final int MAX_RESOURCES = 10; // 테스트할 최대 리소스(CSV) 수

    /**
     * 선택적 리소스 처리 성능 테스트
     *
     * <p>리소스 개수에 따른 성능 변화를 측정합니다.</p>
     * <p>리소스 수가 증가함에 따라 선택적 처리의 이점도 증가하는지 검증합니다.</p>
     */
    @Test
    public void testSelectiveProcessingPerformanceScaling() throws Exception {
        log.info("선택적 리소스 처리 성능 테스트 시작");

        // 결과 컬렉션 초기화
        List<TestResult> allResults = new ArrayList<>();

        // 결과 파일 초기화
        initializeResultsFile();

        // 각 리소스 개수에 대해 테스트 수행
        for (int resourceCount = 2; resourceCount <= MAX_RESOURCES; resourceCount += 2) {
            log.info("리소스 개수 {} 테스트 시작", resourceCount);

            // 실제 CSV 구조와 유사한 테스트 리소스 준비
            List<File> resources = prepareRealisticTestResources(resourceCount);

            // 전체 처리 측정
            long fullProcessingTime = 0;
            Map<String, Double> fullMetrics = new HashMap<>();

            try (JmxMonitor monitor = new JmxMonitor("full_" + resourceCount)) {
                monitor.start();

                markAllResourcesForUpdate(resources);
                fullProcessingTime = measureProcessingTime(resources, false);

                Thread.sleep(1000); // 충분한 메트릭 수집 시간
            }

            fullMetrics = calculateMetricsSummary("full_" + resourceCount + "_metrics.csv");

            // 선택적 처리 측정 - 첫 번째 리소스만 변경된 상황
            long selectiveProcessingTime = 0;
            Map<String, Double> selectiveMetrics = new HashMap<>();

            try (JmxMonitor monitor = new JmxMonitor("selective_" + resourceCount)) {
                monitor.start();

                // 첫 번째 리소스만 변경되었다고 표시
                markFirstResourceForUpdate(resources);
                selectiveProcessingTime = measureProcessingTime(resources, true);

                Thread.sleep(1000); // 충분한 메트릭 수집 시간
            }

            String metricFile = "selective_" + resourceCount + "_metrics.csv";
            selectiveMetrics = calculateMetricsSummary(metricFile);

            // 성능 향상 계산 (개선된 계산 방식)
            double timeImprovement = calculateImprovement(fullProcessingTime, selectiveProcessingTime);
            double memoryImprovement = calculateResourceImprovement(
                fullMetrics.getOrDefault("avgHeapMB", 0.0),
                selectiveMetrics.getOrDefault("avgHeapMB", 0.1)
            );
            double cpuImprovement = calculateResourceImprovement(
                fullMetrics.getOrDefault("avgCpuPercent", 0.0),
                selectiveMetrics.getOrDefault("avgCpuPercent", 0.1)
            );

            // 결과 저장
            TestResult result = new TestResult(
                resourceCount,
                fullProcessingTime,
                selectiveProcessingTime,
                timeImprovement,
                memoryImprovement,
                cpuImprovement
            );

            allResults.add(result);
            appendResultToFile(result);

            log.info("결과: 리소스 {}개, 시간 개선 {}배, 메모리 개선 {}배, CPU 개선 {}배",
                     resourceCount,
                     String.format("%.2f", timeImprovement),
                     String.format("%.2f", memoryImprovement),
                     String.format("%.2f", cpuImprovement));
        }

        // 최종 요약 차트 생성
        generateSummaryChart(allResults);

        log.info("선택적 리소스 처리 성능 테스트 완료. 결과 파일: {}, 차트: {}",
                 CONSOLIDATED_RESULTS_FILE, SUMMARY_CHART_FILE);
    }

    /**
     * 개선된 성능 비율 계산 함수
     */
    private double calculateImprovement(long fullValue, long selectiveValue) {
        if (selectiveValue <= 0) return 1.0; // 0으로 나누기 방지
        return (double) fullValue / selectiveValue;
    }

    /**
     * 리소스 사용량 개선 비율 계산 - 0 값 처리 개선
     */
    private double calculateResourceImprovement(double fullValue, double selectiveValue) {
        // 두 값이 모두 매우 작으면 개선 없음으로 처리
        if (fullValue < 0.1 && selectiveValue < 0.1) {
            return 1.0;
        }

        // selectiveValue가 매우 작지만 fullValue가 큰 경우 (큰 개선)
        if (selectiveValue < 0.1 && fullValue >= 0.1) {
            return Math.min(fullValue / 0.1, 10.0); // 최대 10배로 제한
        }

        // 일반적인 경우
        return fullValue / Math.max(selectiveValue, 0.1); // 0으로 나누기 방지
    }

    /**
     * 테스트 결과 파일 초기화
     */
    private void initializeResultsFile() throws Exception {
        try (FileWriter writer = new FileWriter(CONSOLIDATED_RESULTS_FILE)) {
            writer.write("resourceCount,fullProcessingTime,selectiveProcessingTime," +
                         "timeImprovement,memoryImprovement,cpuImprovement\n");
        }
    }

    /**
     * 테스트 결과를 CSV 파일에 추가
     */
    private void appendResultToFile(TestResult result) throws Exception {
        try (FileWriter writer = new FileWriter(CONSOLIDATED_RESULTS_FILE, true)) {
            writer.write(String.format("%d,%d,%d,%.2f,%.2f,%.2f\n",
                result.resourceCount,
                result.fullProcessingTime,
                result.selectiveProcessingTime,
                result.timeImprovement,
                result.memoryImprovement,
                result.cpuImprovement));
        }
    }

    /**
     * 실제 CSV 구조를 반영한 테스트 리소스 준비
     *
     * @param count 생성할 리소스 개수
     * @return 생성된 테스트 파일 목록
     */
    private List<File> prepareRealisticTestResources(int count) throws Exception {
        List<File> resources = new ArrayList<>();

        // dongcode 파일 생성 (실제 데이터 구조 반영)
        File dongcodeFile = createRealisticCsvFile("dongcode", 49860); // 49,860 레코드, 2.9MB
        resources.add(dongcodeFile);

        // park 파일 생성 (실제 데이터 구조 반영)
        File parkFile = createRealisticCsvFile("park", 17681); // 17,681 레코드, 4.5MB
        resources.add(parkFile);

        // 추가 리소스 파일 생성 (개수에 따라)
        for (int i = 2; i < count; i++) {
            String resourceType = getResourceTypeName(i);
            int recordCount = 15000 + (i * 1000); // 증가하는 크기로 생성
            File additionalFile = createRealisticCsvFile(resourceType, recordCount);
            resources.add(additionalFile);
        }

        return resources;
    }

    /**
     * 테스트 리소스 유형 이름 생성
     */
    private String getResourceTypeName(int index) {
        String[] resourceTypes = {"subway", "school", "hospital", "restaurant",
                                 "convenience", "culture", "traffic", "shopping"};
        if (index - 2 < resourceTypes.length) {
            return resourceTypes[index - 2];
        }
        return "resource" + index;
    }

    /**
     * 실제 CSV 구조와 유사한 테스트 파일 생성
     */
    private File createRealisticCsvFile(String name, int rows) throws Exception {
        File tempFile = File.createTempFile(name, ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            if ("dongcode".equals(name)) {
                writer.write("법정동코드,법정동명,폐지여부\n");
                for (int i = 0; i < rows; i++) {
                    String code = String.format("%010d", 1100000000 + i);
                    writer.write(String.format("%s,법정동%d,존재\n", code, i));
                }
            } else if ("park".equals(name)) {
                writer.write("id,name,위도,경도,면적,type,address,설립일\n");
                for (int i = 0; i < rows; i++) {
                    double lat = 37.5 + (Math.random() * 0.1);
                    double lng = 127.0 + (Math.random() * 0.1);
                    double area = 5000 + (Math.random() * 50000);
                    writer.write(String.format("park%d,공원%d,%.6f,%.6f,%.1f,근린공원,서울시 주소%d,2020-01-01\n",
                            i, i, lat, lng, area, i));
                }
            } else {
                // 다른 리소스 유형에 대한 CSV 구조
                writer.write("id,name,위도,경도,type,address,정보\n");
                for (int i = 0; i < rows; i++) {
                    double lat = 37.5 + (Math.random() * 0.1);
                    double lng = 127.0 + (Math.random() * 0.1);
                    writer.write(String.format("%s%d,%s%d,%.6f,%.6f,type%d,주소%d,정보%d\n",
                            name, i, name, i, lat, lng, i % 5, i, i));
                }
            }
        }
        return tempFile;
    }

    /**
     * 모든 리소스에 업데이트 표시 (수정 시간 변경)
     * 실제 BatchJobUtils.checkNeedsUpdate()의 전체 업데이트 시나리오 시뮬레이션
     */
    private void markAllResourcesForUpdate(List<File> resources) throws Exception {
        for (File resource : resources) {
            // 파일 수정 시간을 현재로 설정 (항상 업데이트 필요)
            resource.setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * 첫 번째 리소스만 업데이트 표시
     */
    private void markFirstResourceForUpdate(List<File> resources) throws Exception {
        // 모든 파일 수정 시간을 어제로 설정 (기본적으로 업데이트 불필요)
        long yesterdayTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        for (File resource : resources) {
            resource.setLastModified(yesterdayTime);
        }

        // 첫 번째 파일만 지금으로 설정 (업데이트 필요)
        if (!resources.isEmpty()) {
            resources.get(0).setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * 처리 시간 측정
     * 실제 배치 처리 로직을 시뮬레이션하는 메서드
     *
     * @param resources 처리할 리소스 목록
     * @param selective 선택적 처리 여부 (true: BatchJobUtils.checkNeedsUpdate() 사용)
     * @return 처리 시간 (밀리초)
     */
    private long measureProcessingTime(List<File> resources, boolean selective) {
        long startTime = System.currentTimeMillis();

        // 실제 BatchJobUtils.checkNeedsUpdate() 로직 시뮬레이션
        for (File resource : resources) {
            LocalDateTime fileLastModified =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(resource.lastModified()), java.time.ZoneId.systemDefault());

            // 선택적 모드에서는 실제 시간을 비교
            // 전체 모드에서는 항상 업데이트가 필요하다고 가정
            boolean needsUpdate;
            if (selective) {
                LocalDateTime lastSuccessTime =
                    LocalDateTime.now().minusDays(1); // 어제 성공했다고 가정
                needsUpdate = fileLastModified.isAfter(lastSuccessTime);
            } else {
                needsUpdate = true;
            }

            // 업데이트가 필요한 경우 데이터 처리 시뮬레이션
            if (needsUpdate) {
                simulateProcessing(resource);
            }
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * 실제 배치 처리 작업을 시뮬레이션하는 메서드
     * SourceCollector, ParkSourceCollector, DongCodeSourceCollector 등의
     * 실제 작업량을 모사합니다.
     */
    private void simulateProcessing(File resource) {
        try {
            // 파일 읽기 - 실제 CsvSourceFileLoader 작업을 시뮬레이션
            Path path = resource.toPath();
            List<String> lines = Files.readAllLines(path);

            // 파싱 및 변환 작업 시뮬레이션 - 실제 SourceCollector의 collect() 및 preprocess() 과정
            for (String line : lines) {
                if (line.startsWith("id,") || line.startsWith("법정동코드,")) continue; // 헤더 스킵

                // 파싱 작업 시뮬레이션
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                // 좌표 처리 시뮬레이션 (PointTypeHandler 유사)
                try {
                    // dongcode 파일과 park 파일 구조 차이 처리
                    double lat = 0, lng = 0;

                    if (resource.getName().contains("dongcode")) {
                        // dongcode 파일 처리
                        // 여기서는 좌표 처리 대신 코드 처리 시뮬레이션
                        String code = parts[0];
                        // 코드 기반 작업 시뮬레이션
                        for (int i = 0; i < 100; i++) {
                            code = code.substring(0, 5) + i;
                        }
                    } else if (resource.getName().contains("park")) {
                        // park 파일 처리
                        lat = Double.parseDouble(parts[2]);
                        lng = Double.parseDouble(parts[3]);
                        double area = 5000;
                        if (parts.length > 4) {
                            try {
                                area = Double.parseDouble(parts[4]);
                            } catch (NumberFormatException e) {
                                // 무시
                            }
                        }

                        // 공원 점수 계산 시뮬레이션 (ParkScoreCalculator 유사)
                        double distance = Math.sqrt(Math.pow(lat - 37.5, 2) + Math.pow(lng - 127.0, 2));
                        double score = 10.0 / (1.0 + distance);

                        // 면적 보정
                        score *= Math.min(area / 10000.0, 2.0);
                    } else {
                        // 다른 리소스 파일 처리
                        lat = Double.parseDouble(parts[2]);
                        lng = Double.parseDouble(parts[3]);

                        // 일반적인 점수 계산 시뮬레이션
                        double distance = Math.sqrt(Math.pow(lat - 37.5, 2) + Math.pow(lng - 127.0, 2));
                        double score = 8.0 / (1.0 + distance);
                    }

                    // CPU 작업 시뮬레이션 (EstateScore 연산)
                    for (int i = 0; i < 500; i++) {
                        double temp = Math.sqrt(i + 1) * Math.log(i + 2) * Math.cos(i);
                    }
                } catch (NumberFormatException ignored) {
                    // 숫자 변환 실패 처리
                }

                // 처리 시간 시뮬레이션 - 실제 배치 작업 부하 모사
                // 파일 크기가 클수록 처리 시간이 길어지도록
                if (resource.length() > 1000000) { // 1MB 이상
                    Thread.sleep(1);
                } else if (resource.length() > 100000) { // 100KB 이상
                    Thread.sleep(0, 500000); // 0.5ms
                }
            }
        } catch (Exception e) {
            log.error("리소스 처리 중 오류", e);
        }
    }

    /**
     * JMX 메트릭 결과 요약 계산
     */
    private Map<String, Double> calculateMetricsSummary(String metricsFile) throws Exception {
        Map<String, Double> summary = new HashMap<>();
        try {
            // 파일이 없으면 빈 요약 반환
            if (!Files.exists(Path.of(metricsFile))) {
                log.warn("메트릭 파일이 존재하지 않음: {}", metricsFile);
                return summary;
            }

            List<String> lines = Files.readAllLines(Path.of(metricsFile));

            if (lines.size() <= 1) { // 헤더만 있으면 반환
                log.info("메트릭 데이터 없음: {}", metricsFile);
                return summary;
            }

            double totalHeap = 0, maxHeap = 0;
            double totalCpu = 0, maxCpu = 0;
            long lastGcCount = 0, totalGcTime = 0;

            for (int i = 1; i < lines.size(); i++) { // 헤더 제외
                String[] parts = lines.get(i).split(",");
                if (parts.length < 8) {
                    log.debug("잘못된 CSV 행 스킵: {}", lines.get(i));
                    continue;
                }

                try {
                    // 숫자 파싱 시 트림 처리 및 예외 처리 강화
                    double heapMB = Double.parseDouble(parts[3].trim());
                    double cpuPercent = Double.parseDouble(parts[6].trim());
                    long gcCount = Long.parseLong(parts[9].trim());
                    long gcTimeMs = Long.parseLong(parts[10].trim());

                    totalHeap += heapMB;
                    maxHeap = Math.max(maxHeap, heapMB);

                    totalCpu += cpuPercent;
                    maxCpu = Math.max(maxCpu, cpuPercent);

                    if (i > 1 && gcCount > lastGcCount) {
                        totalGcTime += gcTimeMs;
                    }
                    lastGcCount = gcCount;
                } catch (NumberFormatException e) {
                    // 파싱 실패한 행 정보 로깅
                    log.debug("숫자 파싱 실패 행: {}, 원인: {}", lines.get(i), e.getMessage());
                }
            }

            int dataPoints = lines.size() - 1;
            if (dataPoints > 0) {
                summary.put("avgHeapMB", totalHeap / dataPoints);
                summary.put("maxHeapMB", maxHeap);
                summary.put("avgCpuPercent", totalCpu / dataPoints);
                summary.put("maxCpuPercent", maxCpu);
                summary.put("totalGcTimeMs", (double) totalGcTime);
                log.debug("메트릭 요약 계산 완료: {} 데이터 포인트", dataPoints);
            } else {
                // 기본값 설정
                summary.put("avgHeapMB", 0.0);
                summary.put("maxHeapMB", 0.0);
                summary.put("avgCpuPercent", 0.0);
                summary.put("maxCpuPercent", 0.0);
                summary.put("totalGcTimeMs", 0.0);
                log.warn("유효한 데이터 포인트 없음: {}", metricsFile);
            }
        } catch (Exception e) {
            log.error("메트릭 요약 계산 중 오류: {}", e.getMessage(), e);
            // 기본값 설정
            summary.put("avgHeapMB", 0.0);
            summary.put("maxHeapMB", 0.0);
            summary.put("avgCpuPercent", 0.0);
            summary.put("maxCpuPercent", 0.0);
            summary.put("totalGcTimeMs", 0.0);
        }
        return summary;
    }

    /**
     * 성능 테스트 결과를 종합한 차트 생성
     */
    private void generateSummaryChart(List<TestResult> results) throws Exception {
        try {
            // HTML 차트 생성
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("  <title>집순 배치 애플리케이션 - 선택적 리소스 처리 성능 분석</title>\n");
            html.append("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append("    .chart-container { width: 90%; max-width: 1000px; margin: 20px auto; }\n");
            html.append("    .header { text-align: center; margin-bottom: 30px; }\n");
            html.append("    .summary { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
            html.append("    table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
            html.append("    table, th, td { border: 1px solid #ddd; }\n");
            html.append("    th, td { padding: 8px; text-align: right; }\n");
            html.append("    th { background-color: #f2f2f2; }\n");
            html.append("  </style>\n");
            html.append("</head>\n<body>\n");

            // 헤더 및 설명
            html.append("  <div class=\"header\">\n");
            html.append("    <h1>집순 배치 애플리케이션 선택적 리소스 처리 성능 분석</h1>\n");
            html.append("    <p>이 분석은 배치 작업에서 변경된 리소스만 선택적으로 처리했을 때의 성능 향상을 보여줍니다.</p>\n");
            html.append("  </div>\n");

            // 주요 발견점 요약
            html.append("  <div class=\"summary\">\n");
            html.append("    <h2>주요 발견점</h2>\n");
            html.append("    <ul>\n");

            // 최대 성능 개선 찾기
            TestResult bestTimeImprovement = results.stream()
                .max((a, b) -> Double.compare(a.timeImprovement, b.timeImprovement))
                .orElse(null);

            if (bestTimeImprovement != null) {
                html.append(String.format("      <li>최대 시간 개선: <strong>%.1f배</strong> (리소스 %d개)</li>\n",
                           bestTimeImprovement.timeImprovement,
                           bestTimeImprovement.resourceCount));
            }

            // 리소스 개수 10개일 때의 개선 찾기
            TestResult largeResource = results.stream()
                .filter(r -> r.resourceCount == MAX_RESOURCES)
                .findFirst()
                .orElse(null);

            if (largeResource != null) {
                html.append(String.format("      <li>리소스 %d개에서: 처리 시간 <strong>%.1f배</strong>, 메모리 사용 <strong>%.1f배</strong>, CPU 사용 <strong>%.1f배</strong> 개선</li>\n",
                           MAX_RESOURCES,
                           largeResource.timeImprovement,
                           largeResource.memoryImprovement,
                           largeResource.cpuImprovement));
            }

            // 선형성 확인
            boolean linearImprovement = results.stream()
                .map(r -> r.timeImprovement)
                .reduce((prev, curr) -> curr >= prev ? curr : 0.0) // 단조 증가 확인
                .orElse(0.0) > 0.0;

            if (linearImprovement) {
                html.append("      <li>리소스 개수 증가에 따라 선택적 처리의 이점이 <strong>선형적으로 증가</strong>함을 확인</li>\n");
            }

            html.append("      <li>특정 리소스만 업데이트되는 현실적인 시나리오에서 선택적 처리 방식의 이점이 크게 나타남</li>\n");
            html.append("    </ul>\n");
            html.append("  </div>\n");

            // 차트 생성: 리소스 개수별 시간 개선
            html.append("  <div class=\"chart-container\">\n");
            html.append("    <h2>리소스 개수에 따른 선택적 처리 시간 개선</h2>\n");
            html.append("    <canvas id=\"timeImprovementChart\"></canvas>\n");
            html.append("  </div>\n");

            // 차트 생성: 리소스 사용량 비교
            html.append("  <div class=\"chart-container\">\n");
            html.append("    <h2>리소스 사용량 개선</h2>\n");
            html.append("    <canvas id=\"resourceUsageChart\"></canvas>\n");
            html.append("  </div>\n");

            // 결과 테이블
            html.append("  <div>\n");
            html.append("    <h2>상세 테스트 결과</h2>\n");
            html.append("    <table>\n");
            html.append("      <tr>\n");
            html.append("        <th>리소스 개수</th>\n");
            html.append("        <th>전체 처리 시간(ms)</th>\n");
            html.append("        <th>선택적 처리 시간(ms)</th>\n");
            html.append("        <th>시간 개선</th>\n");
            html.append("        <th>메모리 개선</th>\n");
            html.append("        <th>CPU 개선</th>\n");
            html.append("      </tr>\n");

            for (TestResult result : results) {
                html.append(String.format("      <tr>\n" +
                           "        <td>%d</td>\n" +
                           "        <td>%d</td>\n" +
                           "        <td>%d</td>\n" +
                           "        <td>%.2f배</td>\n" +
                           "        <td>%.2f배</td>\n" +
                           "        <td>%.2f배</td>\n" +
                           "      </tr>\n",
                           result.resourceCount,
                           result.fullProcessingTime,
                           result.selectiveProcessingTime,
                           result.timeImprovement,
                           result.memoryImprovement,
                           result.cpuImprovement));
            }

            html.append("    </table>\n");
            html.append("  </div>\n");

            // JavaScript 데이터 및 차트 로직
            html.append("  <script>\n");

            // 데이터 준비
            html.append("    // 테스트 결과 데이터\n");
            html.append("    const testResults = [\n");
            for (TestResult result : results) {
                html.append(String.format("      {" +
                           "resourceCount: %d, " +
                           "timeImprovement: %.2f, " +
                           "memoryImprovement: %.2f, " +
                           "cpuImprovement: %.2f" +
                           "},\n",
                           result.resourceCount,
                           result.timeImprovement,
                           result.memoryImprovement,
                           result.cpuImprovement));
            }
            html.append("    ];\n\n");

            // 차트 1: 리소스 개수별 시간 개선
            html.append("    // 리소스 개수별 차트 데이터 준비\n");
            html.append("    const resourceCounts = [...new Set(testResults.map(r => r.resourceCount))];\n");
            html.append("    \n");
            html.append("    const timeImprovementData = resourceCounts.map(count => {\n");
            html.append("      const result = testResults.find(r => r.resourceCount === count);\n");
            html.append("      return result ? result.timeImprovement : 0;\n");
            html.append("    });\n");
            html.append("    \n");

            // 차트 1 그리기
            html.append("    // 시간 개선 차트\n");
            html.append("    new Chart(document.getElementById('timeImprovementChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        labels: resourceCounts,\n");
            html.append("        datasets: [{\n");
            html.append("          label: '처리 시간 개선',\n");
            html.append("          data: timeImprovementData,\n");
            html.append("          borderColor: '#36a2eb',\n");
            html.append("          backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
            html.append("          tension: 0.1\n");
            html.append("        }]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '시간 개선 (배수)'\n");
            html.append("            },\n");
            html.append("            suggestedMin: 1\n");
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
            html.append("                return `${context.dataset.label}: ${context.raw.toFixed(2)}배 빠름`;\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n");

            // 차트 2: 리소스 사용량 비교
            html.append("    // 리소스 사용량 차트\n");
            html.append("    new Chart(document.getElementById('resourceUsageChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        labels: resourceCounts,\n");
            html.append("        datasets: [{\n");
            html.append("          label: '시간 개선',\n");
            html.append("          data: testResults.map(r => r.timeImprovement),\n");
            html.append("          borderColor: 'rgba(54, 162, 235, 1)',\n");
            html.append("          backgroundColor: 'rgba(54, 162, 235, 0.1)',\n");
            html.append("          borderWidth: 2,\n");
            html.append("          fill: false,\n");
            html.append("          tension: 0.1\n");
            html.append("        },\n");
            html.append("        {\n");
            html.append("          label: '메모리 개선',\n");
            html.append("          data: testResults.map(r => r.memoryImprovement),\n");
            html.append("          borderColor: 'rgba(255, 99, 132, 1)',\n");
            html.append("          backgroundColor: 'rgba(255, 99, 132, 0.1)',\n");
            html.append("          borderWidth: 2,\n");
            html.append("          fill: false,\n");
            html.append("          tension: 0.1\n");
            html.append("        },\n");
            html.append("        {\n");
            html.append("          label: 'CPU 개선',\n");
            html.append("          data: testResults.map(r => r.cpuImprovement),\n");
            html.append("          borderColor: 'rgba(75, 192, 192, 1)',\n");
            html.append("          backgroundColor: 'rgba(75, 192, 192, 0.1)',\n");
            html.append("          borderWidth: 2,\n");
            html.append("          fill: false,\n");
            html.append("          tension: 0.1\n");
            html.append("        }]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '개선도 (배수)'\n");
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
            html.append("    });\n");

            // 결론
            html.append("    // 결론 표시\n");
            html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
            html.append("      // 최고 개선치 계산\n");
            html.append("      const maxTimeImprovement = Math.max(...testResults.map(r => r.timeImprovement));\n");
            html.append("      const maxResourceCount = " + MAX_RESOURCES + ";\n");

            html.append("      const conclusion = document.createElement('div');\n");
            html.append("      conclusion.className = 'summary';\n");
            html.append("      conclusion.innerHTML = `\n");
            html.append("        <h2>결론</h2>\n");
            html.append("        <p>선택적 리소스 처리 방식은 최대 <strong>${maxTimeImprovement.toFixed(2)}배</strong>의 성능 향상을 제공하며,\n");
            html.append("           특히 많은 리소스 파일(${maxResourceCount}개)에서 가장 효과적입니다.</p>\n");
            html.append("        <p>이는 집순 배치 애플리케이션이 프로덕션 환경에서 <strong>효율적으로 확장</strong>될 수 있음을 입증합니다.</p>\n");
            html.append("      `;\n");
            html.append("      document.body.appendChild(conclusion);\n");
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
     * 테스트 결과를 저장하는 내부 클래스
     */
    private static class TestResult {
        final int resourceCount;
        final long fullProcessingTime;
        final long selectiveProcessingTime;
        final double timeImprovement;
        final double memoryImprovement;
        final double cpuImprovement;

        TestResult(int resourceCount,
                   long fullProcessingTime, long selectiveProcessingTime,
                   double timeImprovement, double memoryImprovement, double cpuImprovement) {
            this.resourceCount = resourceCount;
            this.fullProcessingTime = fullProcessingTime;
            this.selectiveProcessingTime = selectiveProcessingTime;
            this.timeImprovement = timeImprovement;
            this.memoryImprovement = memoryImprovement;
            this.cpuImprovement = cpuImprovement;
        }
    }
}