package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.dtos.responses.NotificationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Data
public class EventPublisher {

    private final Connection connection;
    private final Channel channel;
    private final ObjectMapper objectMapper;
    private final String serviceName;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    public EventPublisher(String serviceName, String rabbitMqHost, int rabbitMqPort,
                          String rabbitMqUsername, String rabbitMqPassword)
            throws IOException, TimeoutException {
        this.serviceName = serviceName;
        this.objectMapper = new ObjectMapper();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqHost);
        factory.setPort(rabbitMqPort);
        factory.setUsername(rabbitMqUsername);
        factory.setPassword(rabbitMqPassword);

        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare("user.events.exchange", "topic", true);

        channel.confirmSelect();
    }


    public void publishEvent(String eventType, PayloadObject payload) throws IOException, InterruptedException, TimeoutException {

        String correlationId = payload.userId() + "_" + eventType + "_" + UUID.randomUUID().toString().substring(0, 8);

        EventMetadata eventMetadata = EventMetadata.builder()
                .eventId(eventType + "_" + UUID.randomUUID().toString().substring(0, 8))
                .eventType(eventType)
                .eventVersion(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .publisher(serviceName)
                .timestamp(Instant.from(LocalDateTime.now()))
                .build();

        Events events = Events.builder()
                .payload(payload)
                .metadata(eventMetadata)
                .build();

        String eventJson = objectMapper.writeValueAsString(events);

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .contentType("application/json")
                .messageId(eventMetadata.eventId())
                .correlationId(eventMetadata.correlationId())
                .timestamp(Date.from(eventMetadata.timestamp()))
                .build();
        boolean isSuccess = false;
        String errorMsg = "";
        try {
            channel.basicPublish("user.events.exchange", eventType, properties, eventJson.getBytes());
            isSuccess = channel.waitForConfirms(5000);
            if (isSuccess) {
                log.info("Event published successfully. Published event payload: {}, event metadata: {}, and event type: {} ",
                        payload, eventMetadata, eventType);
            } else {
                errorMsg = "Publish confirmation failed";
                log.error("Error publish confirmation. Event metadata {}, event type: {}", eventMetadata, eventType);
            }

        } catch (Exception e) {
            isSuccess = false;
            errorMsg = e.getMessage();
            log.error("Failed to publish event. Event metadata: {}, event type: {}, error: {}",
                    eventMetadata, eventType, e.getMessage(), e);
        } finally {

            NotificationResponseDTO responseDTO = NotificationResponseDTO.builder()
                    .eventId(eventMetadata.eventId())
                    .eventType(eventType)
                    .userId(payload.userId())
                    .correlationId(eventMetadata.correlationId())
                    .timestamp(eventMetadata.timestamp())
                    .publisher(eventMetadata.publisher())
                    .payloadJson(eventJson)
                    .isSuccessful(isSuccess)
                    .errorMessage(errorMsg)
                    .build();

            String notificationJson = objectMapper.writeValueAsString(responseDTO);
            AMQP.BasicProperties notificationProps = new AMQP.BasicProperties().builder()
                    .contentType("application/json")
                    .messageId(responseDTO.eventId())
                    .correlationId(responseDTO.correlationId())
                    .timestamp(Date.from(responseDTO.timestamp()))
                    .build();


            channel.basicPublish(
                    "notification.events.exchange",
                    "notification.event",
                    notificationProps,
                    notificationJson.getBytes()
            );

            log.info("Notification event published. Event metadata: {}, event type: {}, with Notification response DTO: {}",
                    eventMetadata, eventType, responseDTO);
        }
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
