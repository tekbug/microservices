package com.athena.v2.notifications.listeners;

import com.athena.v2.libraries.dtos.responses.NotificationResponseDTO;
import com.athena.v2.notifications.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${spring.rabbitmq.notification-queue}")
    public void receiveNotification(String message) {
        try {
            NotificationResponseDTO dto = objectMapper.readValue(message, NotificationResponseDTO.class);
            notificationService.saveNotification(dto);
            log.info("Received and saved notification: {}", dto.eventId());
        } catch (Exception e) {
            log.error("Error processing notification message: {}", message, e);
        }
    }

}
