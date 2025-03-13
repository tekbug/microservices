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
    public TopicExchange userTopicExchange() {
        return new TopicExchange("user-exchange");
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue("user.created", true);
    }

    @Bean
    public Queue userUpdatedQueue() {
        return new Queue("user.updated", true);
    }

    @Bean
    public Queue userDeletedQueue() {
        return new Queue("user.deleted", true);
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue()).to(userTopicExchange()).with("user.created");
    }

    @Bean
    public Binding userUpdatedBinding() {
        return BindingBuilder.bind(userUpdatedQueue()).to(userTopicExchange()).with("user.updated");
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue()).to(userTopicExchange()).with("user.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplateCreation(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
