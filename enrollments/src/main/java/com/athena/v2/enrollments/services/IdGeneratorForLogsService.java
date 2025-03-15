package com.athena.v2.enrollments.services;

import com.athena.v2.enrollments.models.LogCounter;
import com.athena.v2.enrollments.models.LogCounterId;
import com.athena.v2.enrollments.repositories.LogCounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdGeneratorForLogsService {

    private final LogCounterRepository logCounterRepository;

    /**
     * Generates a production-ready performance log ID in the format:
     * "perf-{endpointCode}-{userId}-{freqNum}"
     */
    @Transactional
    public String generatePerformanceLogId(String endpoint, String userId) {
        String endpointCode = extractEndpointCode(endpoint);
        int freqNum = getNextCounter(endpoint, "perf");
        return String.format("perf-%s-%s-%d", endpointCode, userId, freqNum);
    }

    @Transactional
    public String generateActivityLogId(String endpoint, String userId) {
        String endpointCode = extractEndpointCode(endpoint);
        int freqNum = getNextCounter(endpoint, "act");
        return String.format("act-%s-%s-%d", endpointCode, userId, freqNum);
    }


    /**
     * Retrieves and increments the counter for the given endpoint and log type.
     * Uses a pessimistic lock to prevent race conditions.
     */
    @Transactional
    protected int getNextCounter(String endpoint, String logType) {
        LogCounter logCounter = logCounterRepository.findByIdEndpointAndIdLogType(endpoint, logType);
        if (logCounter == null) {
            LogCounterId id = new LogCounterId(endpoint, logType);
            logCounter = new LogCounter(id, 1);
        } else {
            logCounter.setCounter(logCounter.getCounter() + 1);
        }
        logCounterRepository.save(logCounter);
        return logCounter.getCounter();
    }

    /**
     * Extracts a simple code from the endpoint.
     * For example:
     *   "/register" -> "reg"
     *   "/login"    -> "log"
     */
    private String extractEndpointCode(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return "unk";
        }

        int queryIndex = endpoint.indexOf('?');
        if (queryIndex != -1) {
            endpoint = endpoint.substring(0, queryIndex);
        }

        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        String[] splits = endpoint.split("/");

        int baseEndpointLength = 3;

        if (splits.length <= baseEndpointLength) {
            return String.join("/", splits);
        }

        String[] parts;

        String dynamicEndpoints = splits[baseEndpointLength];
        if(dynamicEndpoints.endsWith("{") && dynamicEndpoints.endsWith("}")) {
            parts = Arrays.copyOf(splits, baseEndpointLength);
        } else {
            int count = baseEndpointLength + 1;
            parts = Arrays.copyOf(splits, count);
        }
        return String.join("/", parts);
    }
}
