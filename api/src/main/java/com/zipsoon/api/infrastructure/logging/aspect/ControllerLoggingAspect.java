package com.zipsoon.api.infrastructure.logging.aspect;

import com.zipsoon.api.domain.auth.UserPrincipal;
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
        log.info("[API:IN] {}.{}() - 요청 시작", className, methodName);
        
        // Detailed parameters - DEBUG level
        if (log.isDebugEnabled()) {
            Object[] args = joinPoint.getArgs();
            log.debug("[API:PARAM] {}.{}() - 파라미터: {}", className, methodName, 
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
            log.info("[API:OUT] {}.{}() - 상태: {} - 소요시간: {}ms", 
                     className, methodName, 
                     ((ResponseEntity<?>) result).getStatusCode(),
                     executionTime);
        } else {
            log.info("[API:OUT] {}.{}() - 소요시간: {}ms", className, methodName, executionTime);
        }
        
        return result;
    }
}