package com.athena.v2.students.configs;

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
    public TopicExchange studentTopicExchange() {
        return new TopicExchange("student-exchange");
    }

    @Bean
    public Queue studentCreatedQueue() {
        return new Queue("student.created", true);
    }

    @Bean
    public Queue studentUpdatedQueue() {
        return new Queue("student.updated", true);
    }

    @Bean
    public Queue studentDeletedQueue() {
        return new Queue("student.deleted", true);
    }

    @Bean
    public Binding studentCreatedBinding() {
        return BindingBuilder.bind(studentCreatedQueue()).to(studentTopicExchange()).with("student.created");
    }

    @Bean
    public Binding studentUpdatedBinding() {
        return BindingBuilder.bind(studentUpdatedQueue()).to(studentTopicExchange()).with("student.updated");
    }

    @Bean
    public Binding studentDeletedBinding() {
        return BindingBuilder.bind(studentDeletedQueue()).to(studentTopicExchange()).with("student.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreationForStudents(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
