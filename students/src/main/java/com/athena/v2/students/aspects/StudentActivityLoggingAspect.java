package com.athena.v2.students.aspects;

import com.athena.v2.students.enums.ActionType;
import com.athena.v2.students.models.StudentAnalyticsLogs;
import com.athena.v2.students.repositories.UsersActivityLogsRepository;
import com.athena.v2.students.services.IdGeneratorForLogsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

// import static com.athena.v2.students.aspects.StudentPerformanceLoggingAspect.getUsername;

//@Aspect
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class StudentActivityLoggingAspect {
//
//    private final UsersActivityLogsRepository activityLogsRepository;
//    private final HttpServletRequest httpServletRequest;
//    private final IdGeneratorForLogsService generator;
//
//    @Around("execution(* com.athena.v2.students.services.*.*(..)) && " +
//            "!execution(* com.athena.v2.students.repositories.*.*(..)) && " +
//            "!execution(* com.athena.v2.students.services.IdGeneratorForLogsService.*(..))")
//    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//        String methodName = joinPoint.getSignature().getName();
//        log.debug("getting the log methods now: {}", methodName);
//        Object[] args = joinPoint.getArgs();
//        log.info("Method {} called with arguments: {}", methodName, args);
//        ActionType actionType = determineActionType(methodName);
//        String userId = getUsernameFromToken();
//
//        StudentAnalyticsLogs logs = new StudentAnalyticsLogs();
//        logs.setActivityId(generator.generateActivityLogId(methodName, userId));
//        logs.setActionType(actionType);
//        logs.setApiEndpoint("/" + methodName);
//        logs.setIpAddress(getIpOriginForUser());
//
//        try {
//            Object result = joinPoint.proceed();
//            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
//            logs.setResponseTime(responseTime);
//            logs.setSuccess(true);
//            logs.setActionType(actionType);
//            logs.setActionDetails(List.of(
//                    String.format("%s action is successfully completed. Response time is %s in milliseconds. The Action Type is %s",
//                            methodName, responseTime, actionType)
//                    ));
//            logs.setUserId(userId);
//            activityLogsRepository.saveAndFlush(logs);
//            if (result != null) {
//                log.info("User Activity logging - Method {} returned: {}", methodName, result);
//            } else {
//                log.info("User Activity logging - Method {} completed (void or null return)", methodName);
//            }
//            return result;
//        } catch (Exception e) {
//            log.error("user activity logs got exception. ", e);
//            float responseTime = (System.currentTimeMillis() - startTime) / 1000f;
//            logs.setResponseTime(responseTime);
//            logs.setSuccess(false);
//            logs.setActionDetails(List.of(
//                    String.format("%s action failed. Response time is %s in milliseconds. The action type is %s, and here is the error log: %s"
//                    , methodName, responseTime, actionType, e.getMessage())
//            ));
//            activityLogsRepository.saveAndFlush(logs);
//            throw e;
//        }
//    }
//
//    private ActionType determineActionType(String methodName) {
//        return getActionType(methodName);
//    }
//
//    public static ActionType getActionType(String methodName) {
//        return switch(methodName) {
//            case "registerUser" -> ActionType.USER_REGISTRATION;
//            case "getAllUsers" -> ActionType.USER_FETCH_ALL;
//            case "getUser" -> ActionType.USER_FETCH_BY_USERNAME;
//            case "getUsersByStatus" -> ActionType.USER_FETCH_ALL_BY_STATUS;
//            case "updateUser" -> ActionType.USER_UPDATE;
//            case "reinstateUser" -> ActionType.USER_REINSTATE;
//            case "blockUser" -> ActionType.USER_BLOCK;
//            case "deleteUser" -> ActionType.USER_DELETE;
//            default -> ActionType.DEFAULT_FLAG;
//        };
//    }
//
////    private String getUsernameFromToken() {
////        return getUsername();
////    }
//
//    private String getIpOriginForUser() {
//        String ip = httpServletRequest.getHeader("x-forwarded-for");
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = httpServletRequest.getRemoteAddr();
//        }
//        return ip;
//    }
//}
