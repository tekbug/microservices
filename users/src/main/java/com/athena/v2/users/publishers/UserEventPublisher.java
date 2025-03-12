package com.athena.v2.users.publishers;

import com.athena.v2.libraries.dtos.requests.EventMetadata;
import com.athena.v2.libraries.dtos.requests.Events;
import com.athena.v2.libraries.dtos.requests.PayloadObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
public class UserEventPublisher {

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

    public UserEventPublisher(
            Connection connection,
            Channel channel,
            ObjectMapper objectMapper,
            String serviceName,
            String host,
            String port,
            String username,
            String password) throws IOException, TimeoutException {
        this.connection = connection;
        this.channel = channel;
        this.objectMapper = objectMapper;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(Integer.parseInt(port));
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare("user.events.exchange", "topic", true);
        channel.confirmSelect();
    }

    public void publishEvent(String eventType, PayloadObject payload) throws IOException, InterruptedException, TimeoutException {

        String correlationId = payload.getUserId() + "_" + eventType + "_" + UUID.randomUUID().toString().substring(0, 8);

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
                .messageId(eventMetadata.getEventId())
                .correlationId(eventMetadata.getCorrelationId())
                .timestamp(Date.from(eventMetadata.getTimestamp()))
                .build();
        channel.basicPublish("user.events.exchange", eventType, properties, eventJson.getBytes());

        if(channel.waitForConfirms(5000)) {

            log.info("Event published successfully. Published event payload: {}, event metadata: {}, and event type: {} ", payload, eventMetadata, eventType);
        } else {

        }
    }
}
