package com.athena.v2.students.aspects;

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

import static com.athena.v2.users.aspects.UserActivityLoggingAspect.getActionType;

@Aspect
@Component
@Slf4j
public class StudentPerformanceLoggingAspect {

    private final UsersPerformanceLogsRepository performanceLogsRepository;
    private final HttpServletRequest httpServletRequest;
    private final IdGeneratorForLogsService idGenerator;

    public StudentPerformanceLoggingAspect(UsersPerformanceLogsRepository performanceLogsRepository,
                                           HttpServletRequest httpServletRequest,
                                           IdGeneratorForLogsService idGenerator) {
        this.performanceLogsRepository = performanceLogsRepository;
        this.httpServletRequest = httpServletRequest;
        this.idGenerator = idGenerator;
    }

    @Around("execution(* com.athena.v2.users.services.*.*(..)) && " +
            "!execution(* com.athena.v2.users.repositories.*.*(..)) && " +
            "!execution(* com.athena.v2.users.services.IdGeneratorForLogsService.*(..))")

    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getDeclaringType().getSimpleName();
        String ipAddress = getIpAddress();
        String userId = getUsernameFromToken();
        ActionType actionType = determineActionType(methodName);
        OperationType operationType = determineOperationType(methodName);
        Object[] args = joinPoint.getArgs();
        log.info("Method {} called with arguments: {}", methodName, args);
        float startTime = System.currentTimeMillis();

        UsersPerformanceLogs performanceLogs = new UsersPerformanceLogs();

        try {
            Object result = joinPoint.proceed();
            performanceLogs.setUserId(userId);
            performanceLogs.setPerformanceId(idGenerator.generatePerformanceLogId(methodName, userId));
            performanceLogs.setMethodName(methodName);
            performanceLogs.setServiceClass(className);
            performanceLogs.setAction(actionType);
            performanceLogs.setOperationType(operationType);
            performanceLogs.setIsSucceeded(true);
            if (result != null) {
                log.info("User Performance logging - Method {} returned: {}", methodName, result);
            } else {
                log.info("User Performance logging - Method {} completed (void or null return)", methodName);
            }
            return result;
        } catch(Exception ex) {
            log.error("performance logging failed. Exception: {}", ex.getMessage());
            performanceLogs.setIsSucceeded(false);
            performanceLogs.setIsFailed(true);
            throw ex;
        } finally {
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            performanceLogs.setUsedMemory(1L);
            performanceLogs.setResponseTime(responseTime);
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
        if (authentication != null) {
            JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
            var jwt = token.getToken();
            var claims = jwt.getClaims();
            return claims.get("preferred_username").toString();
        } else {
            throw new RuntimeException("FAILED TO LOAD USERNAME");
        }
    }


}