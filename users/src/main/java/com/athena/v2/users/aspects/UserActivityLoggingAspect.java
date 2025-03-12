package com.athena.v2.users.aspects;

import com.athena.v2.users.enums.ActionType;
import com.athena.v2.users.models.UsersActivityLogs;
import com.athena.v2.users.repositories.UsersActivityLogsRepository;
import com.athena.v2.users.repositories.UsersRepository;
import com.athena.v2.users.services.IdGeneratorForLogsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.athena.v2.users.aspects.UserPerformanceLoggingAspect.getUsername;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivityLoggingAspect {

    private final UsersActivityLogsRepository activityLogsRepository;
    private final HttpServletRequest httpServletRequest;
    private final UsersRepository usersRepository;
    private final IdGeneratorForLogsService generator;

    @Around("execution(* com.athena.v2.users.services.*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        ActionType actionType = determineActionType(methodName);
        String userId = getUsernameFromToken();

        UsersActivityLogs logs = new UsersActivityLogs();
        logs.setUserId(userId);
        logs.setActivityId(generator.generateActivityLogId(methodName, userId));
        logs.setActionType(actionType);
        logs.setApiEndpoint("/" + methodName);
        logs.setIpAddress(getIpOriginForUser());

        try {
            Object result = joinPoint.proceed();
            log.info("user activity result got logged. Result: {}", result);
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            logs.setResponseTime(responseTime);
            logs.setSuccess(true);
            logs.setActionType(actionType);
            logs.setActionDetails(List.of(
                    String.format("%s action is successfully completed. Response time is %s in milliseconds. The Action Type is %s",
                            methodName, responseTime, actionType)
                    ));
            logs.setUserId(usersRepository.findUsersByUserId(userId).get().getUserId());
            activityLogsRepository.save(logs);
            return result;
        } catch (Exception e) {
            log.error("user activity logs got exception. ", e);
            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
            logs.setResponseTime(responseTime);
            logs.setSuccess(false);
            logs.setActionDetails(List.of(
                    String.format("%s action failed. Response time is %s in milliseconds. The action type is %s, and here is the error log: %s"
                    , methodName, responseTime, actionType, e.getMessage())
            ));
            activityLogsRepository.save(logs);
            throw e;
        }
    }

    private ActionType determineActionType(String methodName) {
        return getActionType(methodName);
    }

    public static ActionType getActionType(String methodName) {
        return switch(methodName) {
            case "register-user" -> ActionType.USER_REGISTRATION;
            case "get-all-user" -> ActionType.USER_FETCH_ALL;
            case "get-user" -> ActionType.USER_FETCH_BY_USERNAME;
            case "get-user-status" -> ActionType.USER_FETCH_ALL_BY_STATUS;
            case "update-user" -> ActionType.USER_UPDATE;
            case "reinstate-user" -> ActionType.USER_REINSTATE;
            case "block-user" -> ActionType.USER_BLOCK;
            case "delete-user" -> ActionType.USER_DELETE;
            default -> ActionType.DEFAULT_FLAG;
        };
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
