package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.dtos.requests.EventMetadata;
import com.athena.v2.libraries.dtos.requests.Events;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private final Connection connection;
    private final Channel channel;
    private final ObjectMapper objectMapper;
    private final String queueName;
    private final Map<String, EventHandler> eventHandlers = new ConcurrentHashMap<>();

    public EventConsumer(String serviceName, String queueName, String host, int port,
                         String username, String password)
            throws IOException, TimeoutException {
        this.queueName = queueName;
        this.objectMapper = new ObjectMapper();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.basicQos(10);
    }

    public void registerEventHandler(String eventType, Class<?> payloadClass,
                                     BiConsumer<EventMetadata, Object> handler) {
        eventHandlers.put(eventType, new EventHandler(payloadClass, handler));
    }

    public void startConsuming() throws IOException {
        channel.basicConsume(queueName, false, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                long deliveryTag = envelope.getDeliveryTag();

                try {

                    String message = new String(body, StandardCharsets.UTF_8);
                    Events event = objectMapper.readValue(message, Events.class);
                    EventMetadata metadata = event.metadata();

                    logger.info("Received event: {} (ID: {})", metadata.eventType(), metadata.eventId());


                    EventHandler handler = eventHandlers.get(metadata.eventType());
                    if (handler != null) {

                        Object payload = objectMapper.convertValue(
                                event.payload(),
                                handler.payloadClass()
                        );

                        try {
                            handler.handler().accept(metadata, payload);
                            // acknowledge and pass to the next level
                            channel.basicAck(deliveryTag, false);
                            logger.info("Successfully processed event: {}", metadata.eventId());
                        } catch (Exception e) {
                            logger.error("Error processing event {}: {}", metadata.eventId(), e.getMessage(), e);
                            // retry the queue
                            boolean requeue = shouldRequeue(properties);
                            channel.basicNack(deliveryTag, false, requeue);
                        }
                    } else {
                        logger.warn("No handler registered for event type: {}", metadata.eventType());
                        // acknowledge but log warning
                        channel.basicAck(deliveryTag, false);
                    }
                } catch (Exception e) {
                    logger.error("Error parsing event: {}", e.getMessage(), e);
                    // this is likely a permanent failure (e.g., malformed JSON)
                    channel.basicNack(deliveryTag, false, false);
                }
            }
        });
    }

    private boolean shouldRequeue(AMQP.BasicProperties properties) {

        if (properties.getHeaders() != null && properties.getHeaders().containsKey("x-retry-count")) {
            int retryCount = (int) properties.getHeaders().get("x-retry-count");
            return retryCount < 3; // retry 3 times
        }
        return true;
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
        private record EventHandler(Class<?> payloadClass, BiConsumer<EventMetadata, Object> handler) {}
}
