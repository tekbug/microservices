package com.athena.v2.courses.configs;

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
    public TopicExchange courseTopicExchange() {
        return new TopicExchange("course-exchange");
    }

    @Bean
    public Queue courseCreatedQueue() {
        return new Queue("course.created", true);
    }

    @Bean
    public Queue courseUpdatedQueue() {
        return new Queue("course.updated", true);
    }

    @Bean
    public Queue courseDeletedQueue() {
        return new Queue("course.deleted", true);
    }

    @Bean
    public Binding courseCreatedBinding() {
        return BindingBuilder.bind(courseCreatedQueue()).to(courseTopicExchange()).with("course.created");
    }

    @Bean
    public Binding courseUpdatedBinding() {
        return BindingBuilder.bind(courseUpdatedQueue()).to(courseTopicExchange()).with("course.updated");
    }

    @Bean
    public Binding courseDeletedBinding() {
        return BindingBuilder.bind(courseDeletedQueue()).to(courseTopicExchange()).with("course.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreationForCourses(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
