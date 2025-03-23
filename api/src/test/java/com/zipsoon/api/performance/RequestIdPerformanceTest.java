package com.zipsoon.api.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RequestID 기반 로깅 시스템의 성능 테스트
 *
 * <p>텍스트 기반 검색과 requestId 기반 검색의 소요 시간을 비교합니다.</p>
 */
@Slf4j
public class RequestIdPerformanceTest {
    // 결과 파일 경로
    private static final String RESULTS_FILE = "requestid_performance_results.csv";
    private static final String SUMMARY_CHART_FILE = "requestid_performance_summary.html";

    // 나노초를 밀리초로 변환할 때 정밀도 손실 방지를 위한 상수
    private static final double NANO_TO_MILLIS = 1_000_000.0;

    // 테스트할 로그 크기 배열
    private static final int[] LOG_SIZES = {3000, 30000, 300000, 3000000, 30000000};

    // 로그 생성 설정
    private static final String[] LOG_LEVELS = {"INFO", "DEBUG", "WARN", "ERROR"};
    private static final String[] LOG_COMPONENTS = {
        "EstateController", "EstateService", "ScoreService", "ApiScoreRepository",
        "ParkScoreCalculator", "EstateRepository", "UserRepository", "HttpClient"
    };
    private static final String[] LOG_TEMPLATES = {
        "매물 {} 정보 조회 요청 처리",
        "사용자 {}의 찜 목록 조회 처리",
        "외부 API 호출: {}",
        "데이터베이스 쿼리 실행: {}",
        "요청 처리 완료: {}",
        "스코어링 계산: {}",
        "매물 필터링: {}"
    };

    /**
     * 일반 로그 검색 성능 테스트
     *
     * <p>다양한 로그 크기에서 RequestID 기반 검색과 전체 텍스트 검색의 성능을 비교합니다.</p>
     * <p>집순 API 서비스의 실제 운영 환경을 가정하여 측정하며(1일당 3,000만줄 로그 가정),
     * 절약되는 시간(ms)을 중심으로 결과를 제시합니다.</p>
     */
    @Test
    public void testSearchPerformance() throws Exception {
        log.info("RequestID 로깅 시스템 검색 성능 테스트 시작");

        // 결과 파일 초기화
        initializeResultsFile();
        List<TestResult> allResults = new ArrayList<>();

        // 다양한 로그 크기에 대해 테스트
        for (int logSize : LOG_SIZES) {
            // 여러 번 반복하여 평균 측정
            int repetitions = 10;
            List<TestResult> repetitionResults = new ArrayList<>();

            for (int i = 0; i < repetitions; i++) {
                TestResult result = runSearchTest(logSize);
                repetitionResults.add(result);

                // 진행 상황 로깅
                if ((i + 1) % 2 == 0) {
                    log.debug("로그 크기 {} 테스트 진행 중: {}/{}", logSize, i + 1, repetitions);
                }
            }

            // 평균 계산
            double avgBruteForceTime = repetitionResults.stream()
                .mapToDouble(TestResult::bruteForceTime)
                .average()
                .orElse(0);

            double avgRequestIdTime = repetitionResults.stream()
                .mapToDouble(TestResult::requestIdTime)
                .average()
                .orElse(0);

            double avgTimeSaved = repetitionResults.stream()
                .mapToDouble(TestResult::timeSaved)
                .average()
                .orElse(0);

            TestResult avgResult = new TestResult(
                logSize,
                avgBruteForceTime,
                avgRequestIdTime,
                avgTimeSaved
            );

            allResults.add(avgResult);
            appendResultToFile(avgResult);

            log.info("로그 {}개 검색: 전체검색={}ms, RequestID={}ms, 절약시간={}ms",
                    logSize,
                    String.format("%.2f", avgBruteForceTime),
                    String.format("%.2f", avgRequestIdTime),
                    String.format("%.2f", avgTimeSaved));

            // 각 로그 크기별 테스트 결과 요약 출력
            double minTimeSaved = repetitionResults.stream()
                .mapToDouble(TestResult::timeSaved)
                .min()
                .orElse(0.0);

            double maxTimeSaved = repetitionResults.stream()
                .mapToDouble(TestResult::timeSaved)
                .max()
                .orElse(0.0);

            log.debug("로그 {}개 결과 요약: 절약시간 최소={}ms, 최대={}ms, 평균={}ms",
                    logSize,
                    String.format("%.2f", minTimeSaved),
                    String.format("%.2f", maxTimeSaved),
                    String.format("%.2f", avgTimeSaved));
        }

        // 차트 생성
        generatePerformanceChart(allResults);

        log.info("RequestID 로깅 시스템 검색 성능 테스트 완료");
        log.info("결과 파일: {}, 차트: {}", RESULTS_FILE, SUMMARY_CHART_FILE);
    }

    /**
     * 로그 검색 테스트 실행
     */
    private TestResult runSearchTest(int logSize) {
        try {
            // 테스트 로그 생성
            List<LogEntry> logs = generateTestLogs(logSize);

            // 검색 대상 로그 주입
            String targetRequestId = "req-" + UUID.randomUUID().toString();
            String targetMessage = "매물 상세 정보 조회";
            List<LogEntry> unusedTargetLogs = injectTargetLogs(logs, targetRequestId, targetMessage, 20);

            // RequestID 검색을 위한 인덱스 생성
            Map<String, List<LogEntry>> requestIdIndex = logs.stream()
                .filter(log -> log.requestId != null && !log.requestId.isEmpty())
                .collect(Collectors.groupingBy(log -> log.requestId));

            // 1. 전체 텍스트 검색 (O(n) 선형 복잡도)
            long startBruteForce = System.nanoTime();
            List<LogEntry> unusedBruteForceResults = logs.stream()
                .filter(log -> log.message.contains(targetMessage))
                .toList();
            long bruteForceTimeNano = System.nanoTime() - startBruteForce;
            double bruteForceTime = bruteForceTimeNano / NANO_TO_MILLIS;

            // 2. RequestID 기반 검색 (O(1) 복잡도)
            long startRequestId = System.nanoTime();
            List<LogEntry> unusedRequestIdResults = requestIdIndex.getOrDefault(targetRequestId, Collections.emptyList());
            long requestIdTimeNano = System.nanoTime() - startRequestId;
            double requestIdTime = requestIdTimeNano / NANO_TO_MILLIS;

            // 절약된 시간 계산
            double timeSaved = Math.max(0, bruteForceTime - requestIdTime);

            return new TestResult(logSize, bruteForceTime, requestIdTime, timeSaved);
        } catch (Exception e) {
            log.error("테스트 실행 중 오류: {}", e.getMessage(), e);
            // 오류 시 기본값 반환
            return new TestResult(logSize, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 테스트용 로그 데이터 생성
     */
    private List<LogEntry> generateTestLogs(int count) {
        List<LogEntry> logs = new ArrayList<>(count);
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String level = LOG_LEVELS[random.nextInt(LOG_LEVELS.length)];
            String component = LOG_COMPONENTS[random.nextInt(LOG_COMPONENTS.length)];
            String messageTemplate = LOG_TEMPLATES[random.nextInt(LOG_TEMPLATES.length)];

            // 메시지에 임의의 값 삽입
            String message = messageTemplate.replace("{}", "ID-" + random.nextInt(1000000));

            // 80% 로그에만 RequestID 설정 (실제 환경 시뮬레이션)
            String requestId = random.nextDouble() < 0.8
                ? "req-" + UUID.randomUUID().toString().substring(0, 8)
                : "";

            logs.add(new LogEntry(
                System.currentTimeMillis() + i,
                level,
                component,
                message,
                requestId
            ));
        }

        return logs;
    }

    /**
     * 검색 대상 로그 주입
     */
    private List<LogEntry> injectTargetLogs(List<LogEntry> logs, String targetRequestId, String targetMessage, int count) {
        List<LogEntry> targetLogs = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String message = targetMessage + " (ID: " + i + ")";
            String component = LOG_COMPONENTS[random.nextInt(LOG_COMPONENTS.length)];

            LogEntry targetLog = new LogEntry(
                System.currentTimeMillis() + i * 100L,
                "INFO",
                component,
                message,
                targetRequestId
            );

            targetLogs.add(targetLog);

            // 로그 목록의 임의 위치에 삽입
            int position = random.nextInt(logs.size());
            logs.add(position, targetLog);
        }

        return targetLogs;
    }

    // 결과 파일 관련 메서드 ====================================================

    /**
     * 검색 성능 결과 파일 초기화
     */
    private void initializeResultsFile() throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE)) {
            writer.write("로그크기,브루트포스검색시간,RequestID검색시간,절약시간\n");
        }
    }

    /**
     * 검색 성능 결과 파일에 추가
     */
    private void appendResultToFile(TestResult result) throws Exception {
        try (FileWriter writer = new FileWriter(RESULTS_FILE, true)) {
            writer.write(String.format("%d,%.2f,%.2f,%.2f%n",
                result.logSize(),
                result.bruteForceTime(),
                result.requestIdTime(),
                result.timeSaved()));
        }
    }

    /**
     * 검색 성능 차트 생성
     */
    private void generatePerformanceChart(List<TestResult> results) throws Exception {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("  <title>RequestID 로깅 성능 분석</title>\n");
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
            html.append("    <h1>RequestID 로깅 시스템 검색 성능 분석</h1>\n");
            html.append("    <p>로그 크기에 따른 RequestID 기반 검색과 전체 텍스트 검색의 성능 비교</p>\n");
            html.append("  </div>\n");

            // 주요 발견점 요약
            html.append("  <div class=\"summary\">\n");
            html.append("    <h2>주요 발견점</h2>\n");
            html.append("    <ul>\n");

            // 최대 로그 크기에서의 절약 시간
            TestResult largestTest = results.stream()
                .max(Comparator.comparing(TestResult::logSize))
                .orElse(null);
            html.append(String.format("      <li>%,d개 로그에서 절약 시간: <strong>%.2f ms</strong></li>%n",
                largestTest.logSize(), largestTest.timeSaved()));

            // 알고리즘 복잡도
            html.append("      <li>검색 알고리즘 복잡도: O(n) 선형 → O(1) 상수 시간으로 개선</li>\n");

            html.append("    </ul>\n");
            html.append("  </div>\n");

            // 차트 컨테이너
            html.append("  <div class=\"chart-container\">\n");
            html.append("    <h2>로그 크기에 따른 검색 시간 비교</h2>\n");
            html.append("    <canvas id=\"searchTimeChart\"></canvas>\n");
            html.append("  </div>\n");

            // 절약 시간 차트
            html.append("  <div class=\"chart-container\">\n");
            html.append("    <h2>로그 크기에 따른 절약 시간 (ms)</h2>\n");
            html.append("    <canvas id=\"timeSavedChart\"></canvas>\n");
            html.append("  </div>\n");

            // 결과 테이블
            html.append("  <div>\n");
            html.append("    <h2>상세 측정 결과</h2>\n");
            html.append("    <table>\n");
            html.append("      <tr>\n");
            html.append("        <th>로그 크기</th>\n");
            html.append("        <th>전체 텍스트 검색 (ms)</th>\n");
            html.append("        <th>RequestID 검색 (ms)</th>\n");
            html.append("        <th>절약 시간 (ms)</th>\n");
            html.append("      </tr>\n");

            for (TestResult result : results) {
                html.append(String.format("      <tr>%n" +
                               "        <td>%,d</td>%n" +
                               "        <td>%.2f</td>%n" +
                               "        <td>%.2f</td>%n" +
                               "        <td>%.2f</td>%n" +
                               "      </tr>%n",
                               result.logSize(),
                               result.bruteForceTime(),
                               result.requestIdTime(),
                               result.timeSaved()));
            }

            html.append("    </table>\n");
            html.append("  </div>\n");

            // JavaScript 차트 코드
            html.append("  <script>\n");

            // 데이터 준비
            html.append("    // 테스트 결과 데이터\n");
            html.append("    const searchResults = [\n");
            for (TestResult result : results) {
                html.append(String.format("      {logSize: %d, bruteForceTime: %.2f, requestIdTime: %.2f, timeSaved: %.2f},%n",
                    result.logSize(), result.bruteForceTime(), result.requestIdTime(), result.timeSaved()));
            }
            html.append("    ];\n\n");

            // 검색 시간 차트
            html.append("    // 검색 시간 차트\n");
            html.append("    const logSizes = searchResults.map(r => r.logSize);\n");
            html.append("    const bruteForceTimeData = searchResults.map(r => r.bruteForceTime);\n");
            html.append("    const requestIdTimeData = searchResults.map(r => r.requestIdTime);\n");
            html.append("    \n");
            html.append("    new Chart(document.getElementById('searchTimeChart'), {\n");
            html.append("      type: 'line',\n");
            html.append("      data: {\n");
            html.append("        labels: logSizes.map(size => size.toLocaleString()),\n");
            html.append("        datasets: [{\n");
            html.append("          label: '전체 텍스트 검색 (O(n) 선형)',\n");
            html.append("          data: bruteForceTimeData,\n");
            html.append("          borderColor: 'rgba(255, 99, 132, 1)',\n");
            html.append("          backgroundColor: 'rgba(255, 99, 132, 0.2)',\n");
            html.append("          tension: 0.1\n");
            html.append("        },\n");
            html.append("        {\n");
            html.append("          label: 'RequestID 검색 (O(1) 상수)',\n");
            html.append("          data: requestIdTimeData,\n");
            html.append("          borderColor: 'rgba(54, 162, 235, 1)',\n");
            html.append("          backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
            html.append("          tension: 0.1\n");
            html.append("        }]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '검색 시간 (밀리초)'\n");
            html.append("            },\n");
            html.append("            beginAtZero: true\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '로그 크기'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n");

            // 절약 시간 차트
            html.append("    // 절약 시간 차트\n");
            html.append("    const timeSavedData = searchResults.map(r => r.timeSaved);\n");
            html.append("    \n");
            html.append("    new Chart(document.getElementById('timeSavedChart'), {\n");
            html.append("      type: 'bar',\n");
            html.append("      data: {\n");
            html.append("        labels: logSizes.map(size => size.toLocaleString()),\n");
            html.append("        datasets: [{\n");
            html.append("          label: '절약된 시간 (ms)',\n");
            html.append("          data: timeSavedData,\n");
            html.append("          backgroundColor: 'rgba(75, 192, 192, 0.5)',\n");
            html.append("          borderColor: 'rgb(75, 192, 192)',\n");
            html.append("          borderWidth: 1\n");
            html.append("        }]\n");
            html.append("      },\n");
            html.append("      options: {\n");
            html.append("        scales: {\n");
            html.append("          y: {\n");
            html.append("            beginAtZero: true,\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '절약된 시간 (ms)'\n");
            html.append("            }\n");
            html.append("          },\n");
            html.append("          x: {\n");
            html.append("            title: {\n");
            html.append("              display: true,\n");
            html.append("              text: '로그 크기'\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        },\n");
            html.append("        plugins: {\n");
            html.append("          tooltip: {\n");
            html.append("            callbacks: {\n");
            html.append("              label: function(context) {\n");
            html.append("                const item = searchResults[context.dataIndex];\n");
            html.append("                return [`절약 시간: ${item.timeSaved.toFixed(2)}ms`, \n");
            html.append("                        `전체 검색: ${item.bruteForceTime.toFixed(2)}ms → RequestID: ${item.requestIdTime.toFixed(2)}ms`];\n");
            html.append("              }\n");
            html.append("            }\n");
            html.append("          }\n");
            html.append("        }\n");
            html.append("      }\n");
            html.append("    });\n");

            // 추가 설명 및 결론
            html.append("    // 추가 설명 및 결론\n");
            html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
            html.append("      // 최대 절약 시간과 해당 로그 크기 찾기\n");
            html.append("      const maxTimeSavedResult = searchResults.reduce((max, current) => \n");
            html.append("        current.timeSaved > max.timeSaved ? current : max, searchResults[0]);\n");
            html.append("      \n");
            html.append("      const conclusion = document.createElement('div');\n");
            html.append("      conclusion.className = 'summary';\n");
            html.append("      conclusion.innerHTML = `\n");
            html.append("        <h2>결론</h2>\n");
            html.append("        <p>RequestID 기반 로그 추적 시스템은 문제 진단 및 해결 시간을 단축시킵니다.</p>\n");
            html.append("        <ul>\n");
            html.append("          <li><strong>시간 효율성</strong>: 최대 ${maxTimeSavedResult.timeSaved.toFixed(2)}ms의 검색 시간 절약</li>\n");
            html.append("          <li><strong>알고리즘 복잡도 개선</strong>: O(n) 선형 → O(1) 상수 시간으로 검색 효율성 향상</li>\n");
            html.append("          <li><strong>로그 볼륨 확장성</strong>: 로그 크기가 증가할수록 절약되는 시간 증가</li>\n");
            html.append("        </ul>\n");
            html.append("      `;\n");
            html.append("      document.body.appendChild(conclusion);\n");
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
     * 검색 성능 테스트 결과를 저장하는 레코드
     */
    private record TestResult(
        int logSize,
        double bruteForceTime,
        double requestIdTime,
        double timeSaved
    ) {}

    /**
     * 로그 항목을 표현하는 클래스
     */
    private record LogEntry(
        long timestamp,
        String level,
        String component,
        String message,
        String requestId
    ) {
        public boolean hasRequestId() {
            return requestId != null && !requestId.isEmpty();
        }
    }
}