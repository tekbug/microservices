package com.athena.v2.enrollments.aspects;

import com.athena.v2.enrollments.enums.ActionType;
import com.athena.v2.enrollments.models.EnrollmentActivityLogs;
import com.athena.v2.enrollments.repositories.EnrollmentsActivityLogsRepository;
import com.athena.v2.enrollments.services.IdGeneratorForLogsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.athena.v2.enrollments.aspects.EnrollmentPerformanceLoggingAspect.getUsername;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentActivityLoggingAspect {

    private final EnrollmentsActivityLogsRepository activityLogsRepository;
    private final HttpServletRequest httpServletRequest;
    private final IdGeneratorForLogsService generator;

    @Around("execution(* com.athena.v2.enrollments.services.*.*(..)) && " +
            "!execution(* com.athena.v2.enrollments.repositories.*.*(..)) && " +
            "!execution(* com.athena.v2.enrollments.services.IdGeneratorForLogsService.*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        log.debug("getting the log methods now: {}", methodName);
        Object[] args = joinPoint.getArgs();
        log.info("Method {} called with arguments: {}", methodName, args);
        String userId = getUsernameFromToken();

        EnrollmentActivityLogs logs = new EnrollmentActivityLogs();
        logs.setActivityId(generator.generateActivityLogId(methodName, userId));
        logs.setActionType(null);
        logs.setApiEndpoint("/" + methodName);
        logs.setIpAddress(getIpOriginForUser());

        try {
            Object result = joinPoint.proceed();
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            logs.setResponseTime(responseTime);
            logs.setSuccess(true);
            logs.setActionType(null);
            logs.setActionDetails(List.of(
                    String.format("%s action is successfully completed. Response time is %s in milliseconds.",
                            methodName, responseTime)
                    ));
            logs.setUserId(userId);
            activityLogsRepository.saveAndFlush(logs);
            if (result != null) {
                log.info("User Activity logging - Method {} returned: {}", methodName, result);
            } else {
                log.info("User Activity logging - Method {} completed (void or null return)", methodName);
            }
            return result;
        } catch (Exception e) {
            log.error("user activity logs got exception. ", e);
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            logs.setResponseTime(responseTime);
            logs.setSuccess(false);
            logs.setActionDetails(List.of(
                    String.format("%s action failed. Response time is %s in milliseconds. And here is the error log: %s"
                    , methodName, responseTime, e.getMessage())
            ));
            activityLogsRepository.saveAndFlush(logs);
            throw e;
        }
    }

    private String getUsernameFromToken() {
        return getUsername();
    }

    private String getIpOriginForUser() {
        String ip = httpServletRequest.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }
}
