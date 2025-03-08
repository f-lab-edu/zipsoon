# Zipsoon Project - Logging Improvement Implementation Report

## 1. Introduction

This document outlines a comprehensive strategy for improving logging across the Zipsoon project. The goal is to establish consistent, informative logging that captures key request-response flows in the API and improves visibility into batch processing operations.

### Primary Objectives

- Implement standardized controller request-response logging
- Establish consistent service layer logging
- Improve batch job logging with enhanced statistics
- Eliminate duplicate logging patterns
- Standardize logging format and levels

You may remove or adjust any existing log statements that are no longer necessary due to these changes, and all logs message should be given in Korean.

## 2. Current State Analysis

### API Component

- **RequestIdFilter**: Already well-implemented for tracing requests
- **Controller Layer**: Missing systematic entry/exit logging
- **Service Layer**: Inconsistent logging (e.g., only `EstateService.findEstatesInViewport` logs properly)
- **Exception Handling**: Basic logging exists but lacks detail

### Batch Component

- **Basic logging**: Present but lacks consistency
- **Duplication**: Logging duplicated between `DataPipelineService` and individual jobs
- **Statistics**: Insufficient aggregated statistics for batch operations
- **Standardization**: Lack of common format across different batch components

## 3. API Logging Improvements

### 3.1 Controller Logging Using AOP

Implement an aspect to log controller method entry and exit points, parameters, and response status.

```java
package com.zipsoon.api.infrastructure.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {
    
    @Pointcut("execution(* com.zipsoon.api.interfaces.api..*Controller.*(..))")
    public void controllerMethods() {}
    
    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Request start - INFO level
        log.info("[API:IN] {}.{}() - started", className, methodName);
        
        // Detailed parameters - DEBUG level
        if (log.isDebugEnabled()) {
            Object[] args = joinPoint.getArgs();
            log.debug("[API:PARAM] {}.{}() - params: {}", className, methodName, 
                     Arrays.stream(args)
                         .filter(arg -> !(arg instanceof UserPrincipal)) // Filter security info
                         .collect(Collectors.toList()));
        }
        
        // Method execution and timing
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Response - INFO level
        if (result instanceof ResponseEntity) {
            log.info("[API:OUT] {}.{}() - status: {} - took: {}ms", 
                     className, methodName, 
                     ((ResponseEntity<?>) result).getStatusCode(),
                     executionTime);
        } else {
            log.info("[API:OUT] {}.{}() - took: {}ms", className, methodName, executionTime);
        }
        
        return result;
    }
}
```

### 3.2 Service Layer Logging Standardization

Apply a consistent logging pattern to all service layer methods:

```java
// Example for EstateService
@Slf4j
@Service
@RequiredArgsConstructor
public class EstateService {

    @Transactional(readOnly = true)
    public List<EstateResponse> findEstatesInViewport(ViewportRequest request, Long userId) {
        // Entry logging - DEBUG level
        log.debug("[SVC:IN] findEstatesInViewport(viewport={}, userId={})", 
                formatViewport(request), userId != null ? userId : "guest");
        
        // Validation and business logic...
        
        // Query execution
        var estates = apiEstateRepository.findAllInViewport(request, limit);
        
        // Important business result - INFO level
        if (estates.isEmpty()) {
            log.info("[SVC:RESULT] No estates found in viewport: {}", formatViewport(request));
            return Collections.emptyList();
        }

        log.info("[SVC:RESULT] Found {} estates for viewport (userId: {})", 
            estates.size(), userId != null ? userId : "guest");
        
        // Process results...
        
        // Exit logging - DEBUG level
        log.debug("[SVC:OUT] findEstatesInViewport() completed - returned {} estates", 
                estateResponses.size());
        
        return estateResponses;
    }
    
    // Apply the same pattern to all service methods
}
```

### 3.3 Enhanced Exception Handling Logging

Improve the `GlobalExceptionHandler` to provide more detailed logging:

```java
@ExceptionHandler(ServiceException.class)
protected ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
    // Include error code, message, and details in log
    log.error("[API:ERR] ServiceException: {} - code: {} - details: {}", 
              e.getMessage(), e.getErrorCode(), e.getDetails());
              
    // Existing response generation logic...
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ErrorResponseFactory.from(
            e.getErrorCode(),
            details,
            e.getMessage()
        ));
}

@ExceptionHandler(Exception.class)
protected ResponseEntity<ErrorResponse> handleServerException(Exception e) {
    // Include stack trace in non-production environments
    if (!"prod".equalsIgnoreCase(activeProfile)) {
        log.error("[API:ERR] Unhandled exception in request", e);
    } else {
        log.error("[API:ERR] Unhandled exception: {}", e.getMessage());
    }
    
    // Existing response generation logic...
}
```

## 4. Batch Logging Improvements

### 4.1 Extending PipelineStep Interface

Enhance the `PipelineStep` interface with standardized logging methods:

```java
public interface PipelineStep {
    boolean execute();
    String getStepName();
    
    // Add default logging utility methods
    default void logStepStart() {
        LoggerFactory.getLogger(this.getClass()).info(
            "[BATCH:STEP-START] {} - starting execution", getStepName());
    }
    
    default void logStepEnd(boolean success, long executionTimeMs) {
        LoggerFactory.getLogger(this.getClass()).info(
            "[BATCH:STEP-END] {} - {} - took: {}ms", 
            getStepName(), 
            success ? "SUCCESS" : "FAILURE",
            executionTimeMs);
    }
}
```

### 4.2 Improving DataPipelineService

Refactor the pipeline execution to avoid duplicate logging and add execution statistics:

```java
// DataPipelineService improvement
private void runPipelineSteps(Iterable<PipelineStep> steps) {
    log.info("[BATCH:PIPELINE-START] Beginning pipeline execution");
    
    List<String> succeededSteps = new ArrayList<>();
    List<String> failedSteps = new ArrayList<>();
    Map<String, Long> executionTimes = new HashMap<>();
    long pipelineStartTime = System.currentTimeMillis();

    for (PipelineStep step : steps) {
        String stepName = step.getStepName();
        
        // Step start logging delegated to step implementation
        step.logStepStart();
        
        // Timing and execution
        long startTime = System.currentTimeMillis();
        boolean success = step.execute();
        long executionTime = System.currentTimeMillis() - startTime;
        executionTimes.put(stepName, executionTime);
        
        // Step end logging delegated to step implementation
        step.logStepEnd(success, executionTime);
        
        if (success) {
            succeededSteps.add(stepName);
        } else {
            failedSteps.add(stepName);
        }
    }
    
    long totalExecutionTime = System.currentTimeMillis() - pipelineStartTime;
    
    // Pipeline execution summary with statistics
    log.info("[BATCH:PIPELINE-SUMMARY] Total execution time: {}ms", totalExecutionTime);
    log.info("[BATCH:PIPELINE-SUMMARY] Succeeded steps: {}", succeededSteps);
    log.info("[BATCH:PIPELINE-SUMMARY] Failed steps: {}", failedSteps);
    log.info("[BATCH:PIPELINE-SUMMARY] Step execution times: {}", executionTimes);
    
    log.info("[BATCH:PIPELINE-END] Pipeline execution completed with status: {}", 
            failedSteps.isEmpty() ? "SUCCESS" : "FAILURE");
}
```

### 4.3 Step Execution Listener

Create a listener for Spring Batch steps to capture detailed execution statistics:

```java
@Slf4j
public class StepExecutionLoggingListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[BATCH:JOB-STEP-START] {} - jobId: {}", 
                stepExecution.getStepName(), 
                stepExecution.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[BATCH:JOB-STEP-END] {} - status: {} - read: {} - write: {} - filter: {} - commit: {} - rollback: {}",
                stepExecution.getStepName(),
                stepExecution.getExitStatus().getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());
        
        // Add timing information
        long executionTime = stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime();
        log.info("[BATCH:JOB-STEP-STATS] {} - execution time: {}ms", 
                stepExecution.getStepName(), executionTime);
                
        return stepExecution.getExitStatus();
    }
}
```

Apply this listener to all batch job steps:

```java
// ScoreJobConfig example
@Bean
public Step scoreProcessingStep() {
    return new StepBuilder("scoreProcessingStep", jobRepository)
        .<Estate, List<EstateScore>>chunk(100, transactionManager)
        .reader(scoreReader)
        .processor(scoreProcessor)
        .writer(scoreWriter)
        .listener(new StepExecutionLoggingListener()) // Add listener
        .build();
}
```

## 5. Logging Format Standardization

### 5.1 Standardized Log Prefixes

Implement consistent log prefixes for easier filtering and analysis:

| Component | Prefix Pattern | Example |
|-----------|---------------|---------|
| API Controller Entry | `[API:IN]` | `[API:IN] EstateController.getEstateDetail() - started` |
| API Controller Params | `[API:PARAM]` | `[API:PARAM] EstateController.getEstateDetail() - params: [1, null]` |
| API Controller Exit | `[API:OUT]` | `[API:OUT] EstateController.getEstateDetail() - status: 200 - took: 125ms` |
| API Controller Error | `[API:ERR]` | `[API:ERR] ServiceException: Estate not found - code: 404_001` |
| Service Entry | `[SVC:IN]` | `[SVC:IN] findEstateDetail(id=1, userId=null)` |
| Service Results | `[SVC:RESULT]` | `[SVC:RESULT] Found estate with id: 1` |
| Service Exit | `[SVC:OUT]` | `[SVC:OUT] findEstateDetail() completed` |
| Service Error | `[SVC:ERR]` | `[SVC:ERR] Unable to process estate: Invalid format` |
| Batch Pipeline | `[BATCH:PIPELINE-*]` | `[BATCH:PIPELINE-START] Beginning pipeline execution` |
| Batch Step | `[BATCH:STEP-*]` | `[BATCH:STEP-START] SCORE_CALCULATION - starting execution` |
| Batch Job | `[BATCH:JOB-*]` | `[BATCH:JOB-START] estateJob - starting execution` |

### 5.2 Log Level Guidelines

Apply appropriate log levels based on the information's importance:

| Context | Level | Rationale |
|---------|-------|-----------|
| Controller entry/exit | INFO | Track user requests and system responses |
| Controller parameters | DEBUG | Detailed request data for debugging (may contain sensitive info) |
| Service method entry | DEBUG | Internal method calls for detailed troubleshooting |
| Important business results | INFO | Key outcomes that indicate system function |
| Service method exit | DEBUG | Routine completion information |
| Errors/Exceptions | ERROR | Problems requiring attention |
| Handled exceptions | WARN | Expected issues that still warrant notice |
| Batch operation start/end | INFO | Key system operation milestones |
| Batch statistics | INFO | Operation results summary |
| Detailed batch processing | DEBUG | Step-by-step processing details |

## 6. Implementation Recommendations

### 6.1 Dependencies

Ensure the following dependencies are available:

```gradle
// For AOP support
implementation 'org.springframework.boot:spring-boot-starter-aop'
```

### 6.2 Implementation Order

1. **API Logging Improvements**
   - Implement `ControllerLoggingAspect`
   - Update service layer logging patterns
   - Enhance exception handler logging

2. **Batch Logging Improvements**
   - Extend `PipelineStep` interface
   - Update `DataPipelineService` implementation
   - Create and apply `StepExecutionLoggingListener`

3. **Verification**
   - Test API endpoints and verify consistent logging
   - Run complete batch pipeline and verify statistics
   - Check log levels are appropriate for different environments

### 6.3 Configuration Updates

Update application.yml for appropriate log levels:

```yaml
logging:
  level:
    root: INFO
    com.zipsoon.api.interfaces.api: INFO  # API Controllers
    com.zipsoon.api.application: DEBUG     # Service layer
    com.zipsoon.batch.job: INFO           # Batch jobs
    com.zipsoon.batch.application: DEBUG  # Batch processing
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{X-Request-Id}] %-5level %logger{36} - %msg%n"
```

## 7. Conclusion

This logging improvement plan will provide significant visibility into the system's operation while maintaining a clean, consistent format. Proper implementation will help with debugging, monitoring, and understanding system behavior in both development and production environments.

By following these guidelines, the Zipsoon project will have enhanced observability with minimal overhead and zero overengineering, focusing on practical information that's most valuable to developers and operations teams.