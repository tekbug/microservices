package com.athena.v2.teachers.aspects;

import com.athena.v2.teachers.models.TeacherPerformanceLogs;
import com.athena.v2.teachers.repositories.TeachersPerformanceLogsRepository;
import com.athena.v2.teachers.services.IdGeneratorForLogsService;
import com.athena.v2.teachers.enums.ActionType;
import com.athena.v2.teachers.enums.OperationType;
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

import static com.athena.v2.teachers.aspects.TeacherActivityLoggingAspect.getActionType;

@Aspect
@Component
@Slf4j
public class TeacherPerformanceLoggingAspect {

    private final TeachersPerformanceLogsRepository performanceLogsRepository;
    private final HttpServletRequest httpServletRequest;
    private final IdGeneratorForLogsService idGenerator;

    public TeacherPerformanceLoggingAspect(TeachersPerformanceLogsRepository performanceLogsRepository,
                                           HttpServletRequest httpServletRequest,
                                           IdGeneratorForLogsService idGenerator) {
        this.performanceLogsRepository = performanceLogsRepository;
        this.httpServletRequest = httpServletRequest;
        this.idGenerator = idGenerator;
    }

    @Around("execution(* com.athena.v2.teachers.services.*.*(..)) && " +
            "!execution(* com.athena.v2.teachers.repositories.*.*(..)) && " +
            "!execution(* com.athena.v2.teachers.services.IdGeneratorForLogsService.*(..))")

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

        TeacherPerformanceLogs performanceLogs = new TeacherPerformanceLogs();

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