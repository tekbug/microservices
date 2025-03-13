package com.athena.v2.notifications.services;

import com.athena.v2.libraries.dtos.responses.NotificationResponseDTO;
import com.athena.v2.notifications.exceptions.NotificationNotFoundException;
import com.athena.v2.notifications.models.Notifications;
import com.athena.v2.notifications.repositories.NotificationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getAllNotificationsForUser() {
        String userId = extractUsernameFromToken();
        List<Notifications> notificationsList = fetchNotification(userId);
        return notificationsList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public String extractUsernameFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            String userId = jwt.getClaimAsString("preferred_username");
            assert userId != null;
            return userId;
        } else {
            throw new NotificationNotFoundException("NOTIFICATION IS NOT FOUND FOR THE GIVEN USER BECAUSE THE USER IS NOT FOUND");
        }
    }

    private List<Notifications> fetchNotification(String userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public void saveNotification(NotificationResponseDTO dto) {
        Notifications entity = Notifications.builder()
                .eventId(dto.eventId())
                .eventType(dto.eventType())
                .userId(dto.userId())
                .correlationId(dto.correlationId())
                .timestamp(dto.timestamp())
                .publisher(dto.publisher())
                .payloadJson(dto.payloadJson())
                .isSuccessful(dto.isSuccessful())
                .errorMessage(dto.errorMessage())
                .build();

        notificationRepository.saveAndFlush(entity);
        log.info("Saved notification: {}", entity.getEventId());
    }

    private NotificationResponseDTO mapToDto(@NonNull Notifications notifications) {
        return NotificationResponseDTO.builder()
                .userId(notifications.getUserId())
                .eventId(notifications.getEventId())
                .eventType(notifications.getEventType())
                .correlationId(notifications.getCorrelationId())
                .publisher(notifications.getPublisher())
                .timestamp(notifications.getTimestamp())
                .payloadJson(notifications.getPayloadJson())
                .isSuccessful(notifications.isSuccessful())
                .errorMessage(notifications.getErrorMessage())
                .build();
    }
}
