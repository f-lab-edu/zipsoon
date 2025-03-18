package com.zipsoon.batch.performance;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.lang.management.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JMX를 통한 성능 메트릭 수집 모니터
 *
 * <p>JVM 메모리, CPU, 스레드, GC 정보를 모니터링하고 CSV 파일로 저장합니다.</p>
 * <p>시작/종료 시 AutoClosable 인터페이스를 통해 리소스를 안전하게 관리합니다.</p>
 */
@Slf4j
public class JmxMonitor implements AutoCloseable {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<MetricSample> samples = Collections.synchronizedList(new ArrayList<>());
    private final String testName;
    private final String resultFile;
    private final long startTime;

    /**
     * 모니터 생성
     *
     * @param testName 테스트 식별자 (결과 파일 이름에 사용됨)
     */
    public JmxMonitor(String testName) {
        this.testName = testName;
        this.resultFile = testName + "_metrics.csv";
        this.startTime = System.currentTimeMillis();
        initResultFile();
    }

    /**
     * 메트릭 모니터링 시작
     * 1초 간격으로 정보를 수집합니다.
     */
    public void start() {
        log.debug("JMX 모니터링 시작: {}", testName);
        scheduler.scheduleAtFixedRate(this::collectMetrics, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * JMX를 통한 메트릭 수집
     */
    private void collectMetrics() {
        try {
            // 경과 시간 (초)
            long elapsedTimeMs = System.currentTimeMillis() - startTime;

            // JVM 메모리 사용량
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

            // CPU 사용량
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = -1;
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
            }

            // 스레드 수
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();

            // GC 정보
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            long gcCount = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
            long gcTime = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();

            samples.add(new MetricSample(
                System.currentTimeMillis(),
                elapsedTimeMs,
                heapUsed,
                heapMax,
                nonHeapUsed,
                cpuLoad,
                threadCount,
                peakThreadCount,
                gcCount,
                gcTime
            ));
        } catch (Exception e) {
            log.error("메트릭 수집 중 오류", e);
        }
    }

    @Override
    public void close() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
            exportMetrics();
            log.debug("JMX 모니터링 종료: {}, 샘플 수: {}", testName, samples.size());
        } catch (Exception e) {
            log.error("모니터 종료 중 오류", e);
        }
    }

    /**
     * 수집된 메트릭을 CSV 파일로 저장
     */
    private void exportMetrics() {
        try (FileWriter writer = new FileWriter(resultFile, true)) {
            for (MetricSample sample : samples) {
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // 형식화된 소수점 표현을 위해 Locale.US 사용
                // 이렇게 하면 시스템 로케일과 상관없이 소수점이 마침표(.)로 표시됨
                writer.write(String.format(java.util.Locale.US,
                    "%s,%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%d\n",
                    testName,
                    sample.timestamp,
                    sample.elapsedTimeMs / 1000, // 초 단위로 변환
                    sample.heapUsed / (1024*1024),
                    sample.heapMax / (1024*1024),
                    sample.nonHeapUsed / (1024*1024),
                    sample.cpuLoad,
                    sample.threadCount,
                    sample.peakThreadCount,
                    sample.gcCount,
                    sample.gcTime));
            }
            log.debug("메트릭 내보내기 완료: {} 샘플", samples.size());
        } catch (Exception e) {
            log.error("메트릭 내보내기 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 결과 파일 초기화
     */
    private void initResultFile() {
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write("testName,timestamp,elapsedSecs,heapMB,heapMaxMB,nonHeapMB," +
                         "cpuPercent,threads,peakThreads,gcCount,gcTimeMs\n");
        } catch (Exception e) {
            log.error("결과 파일 초기화 중 오류", e);
        }
    }

    /**
     * 메트릭 샘플 내부 클래스
     */
    private static class MetricSample {
        final long timestamp;
        final long elapsedTimeMs;
        final long heapUsed;
        final long heapMax;
        final long nonHeapUsed;
        final double cpuLoad;
        final int threadCount;
        final int peakThreadCount;
        final long gcCount;
        final long gcTime;

        MetricSample(long timestamp, long elapsedTimeMs, long heapUsed, long heapMax,
                    long nonHeapUsed, double cpuLoad, int threadCount,
                    int peakThreadCount, long gcCount, long gcTime) {
            this.timestamp = timestamp;
            this.elapsedTimeMs = elapsedTimeMs;
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.cpuLoad = cpuLoad;
            this.threadCount = threadCount;
            this.peakThreadCount = peakThreadCount;
            this.gcCount = gcCount;
            this.gcTime = gcTime;
        }
    }
}