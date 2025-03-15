package com.athena.v2.enrollments.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public TopicExchange enrollmentTopicExchange() {
        return new TopicExchange("enrollment-exchange");
    }

    @Bean
    public Queue enrollmentCreatedQueue() {
        return new Queue("enrollment.created", true);
    }

    @Bean
    public Queue enrollmentUpdatedQueue() {
        return new Queue("enrollment.updated", true);
    }

    @Bean
    public Queue enrollmentDeletedQueue() {
        return new Queue("enrollment.deleted", true);
    }

    @Bean
    public Binding enrollmentCreatedBinding() {
        return BindingBuilder.bind(enrollmentCreatedQueue()).to(enrollmentTopicExchange()).with("enrollment.created");
    }

    @Bean
    public Binding enrollmentUpdatedBinding() {
        return BindingBuilder.bind(enrollmentUpdatedQueue()).to(enrollmentTopicExchange()).with("enrollment.updated");
    }

    @Bean
    public Binding enrollmentDeletedBinding() {
        return BindingBuilder.bind(enrollmentDeletedQueue()).to(enrollmentTopicExchange()).with("enrollment.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreationForEnrollments(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
