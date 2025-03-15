package com.athena.v2.assignments.configs;

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
    public TopicExchange assignmentTopicExchange() {
        return new TopicExchange("assignment-exchange");
    }

    @Bean
    public Queue assignmentCreatedQueue() {
        return new Queue("assignment.created", true);
    }

    @Bean
    public Queue assignmentUpdatedQueue() {
        return new Queue("assignment.updated", true);
    }

    @Bean
    public Queue assignmentDeletedQueue() {
        return new Queue("assignment.deleted", true);
    }

    @Bean
    public Binding assignmentCreatedBinding() {
        return BindingBuilder.bind(assignmentCreatedQueue()).to(assignmentTopicExchange()).with("assignment.created");
    }

    @Bean
    public Binding assignmentUpdatedBinding() {
        return BindingBuilder.bind(assignmentUpdatedQueue()).to(assignmentTopicExchange()).with("assignment.updated");
    }

    @Bean
    public Binding assignmentDeletedBinding() {
        return BindingBuilder.bind(assignmentDeletedQueue()).to(assignmentTopicExchange()).with("assignment.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreationForAssignments(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
