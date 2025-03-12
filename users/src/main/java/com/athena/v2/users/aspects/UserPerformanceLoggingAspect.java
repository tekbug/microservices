package com.athena.v2.users.aspects;

import com.athena.v2.users.enums.ActionType;
import com.athena.v2.users.enums.OperationType;
import com.athena.v2.users.models.UsersPerformanceLogs;
import com.athena.v2.users.repositories.UsersPerformanceLogsRepository;
import com.athena.v2.users.services.IdGeneratorForLogsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.management.MemoryMXBean;

import static com.athena.v2.users.aspects.UserActivityLoggingAspect.getActionType;

@Aspect
@Component
@Slf4j
public class UserPerformanceLoggingAspect {

    private final UsersPerformanceLogsRepository performanceLogsRepository;
    private final HttpServletRequest httpServletRequest;
    private final IdGeneratorForLogsService idGenerator;
    private final MemoryMXBean memoryMXBean;

    public UserPerformanceLoggingAspect(UsersPerformanceLogsRepository performanceLogsRepository,
                                        MemoryMXBean memoryMXBean,
                                        HttpServletRequest httpServletRequest,
                                        IdGeneratorForLogsService idGenerator) {
        this.performanceLogsRepository = performanceLogsRepository;
        this.memoryMXBean = memoryMXBean;
        this.httpServletRequest = httpServletRequest;
        this.idGenerator = idGenerator;
    }

    private static final long PERFORMANCE_LOGGING_THRESHOLD_IN_MS = 1000;

    @Around("execution(* com.athena.v2.users.services.*.*(..))")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String userId = getUsernameFromToken();
        String className = signature.getDeclaringType().getSimpleName();
        String ipAddress = getIpAddress();
        long memoryInit = memoryMXBean.getHeapMemoryUsage().getUsed();
        ActionType actionType = determineActionType(methodName);
        OperationType operationType = determineOperationType(methodName);
        float startTime = System.currentTimeMillis();

        UsersPerformanceLogs performanceLogs = new UsersPerformanceLogs();

        try {
            Object result = joinPoint.proceed();
            log.info("performance logging got logged. Result: {}", result);
            performanceLogs.setUserId(userId);
            performanceLogs.setPerformanceId(idGenerator.generatePerformanceLogId(methodName, userId));
            performanceLogs.setMethodName(methodName);
            performanceLogs.setServiceClass(className);
            performanceLogs.setAction(actionType);
            performanceLogs.setOperationType(operationType);
            performanceLogs.setIsSucceeded(true);
            return result;
        } catch(Exception ex) {
            log.error("performance logging failed. Exception: {}", ex.getMessage());
            performanceLogs.setIsSucceeded(false);
            performanceLogs.setIsFailed(true);
            throw ex;
        } finally {
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            long memoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed() - memoryInit;
            performanceLogs.setUsedMemory(memoryUsed);
            performanceLogs.setResponseTime(responseTime);
            boolean isThresholdExceeded = (responseTime - startTime) > PERFORMANCE_LOGGING_THRESHOLD_IN_MS;
            performanceLogs.setThresholdExceeded(isThresholdExceeded);
            performanceLogs.setIpAddress(ipAddress);
            performanceLogsRepository.saveAndFlush(performanceLogs);
        }
    }

    private ActionType determineActionType(String methodName) {
        return getActionType(methodName);
    }

    private OperationType determineOperationType(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("return") || methodName.startsWith("find") || methodName.startsWith("search")) {
            return OperationType.READ;
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return OperationType.DELETE;
        } else if (methodName.startsWith("save") || methodName.startsWith("update") ||
                methodName.startsWith("create") || methodName.startsWith("register") ||
                methodName.startsWith("add")) {
            return OperationType.WRITE;
        } else {
            return OperationType.UNKNOWN;
        }
    }

    private String getUsernameFromToken() {
        return getUsername();
    }

    private String getIpAddress() {
        String ip = httpServletRequest.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }


    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        var jwt = token.getToken();
        var claims = jwt.getClaims();
        String userId = claims.get("preferred_username").toString();
        assert userId != null;
        return userId;
    }

}