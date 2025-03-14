package com.athena.v2.teachers.configs;

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
    public TopicExchange teacherTopicExchange() {
        return new TopicExchange("teacher-exchange");
    }

    @Bean
    public Queue teacherCreatedQueue() {
        return new Queue("teacher.created", true);
    }

    @Bean
    public Queue teacherUpdatedQueue() {
        return new Queue("teacher.updated", true);
    }

    @Bean
    public Queue teacherDeletedQueue() {
        return new Queue("teacher.deleted", true);
    }

    @Bean
    public Binding teacherCreatedBinding() {
        return BindingBuilder.bind(teacherCreatedQueue()).to(teacherTopicExchange()).with("teacher.created");
    }

    @Bean
    public Binding teacherUpdatedBinding() {
        return BindingBuilder.bind(teacherUpdatedQueue()).to(teacherTopicExchange()).with("teacher.updated");
    }

    @Bean
    public Binding teacherDeletedBinding() {
        return BindingBuilder.bind(teacherDeletedQueue()).to(teacherTopicExchange()).with("teacher.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreationForTeachers(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
