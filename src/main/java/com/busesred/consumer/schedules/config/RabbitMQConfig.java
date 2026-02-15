package com.busesred.consumer.schedules.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.schedules}")
    private String exchangeName;

    @Value("${rabbitmq.queue.schedules}")
    private String queueName;

    @Value("${rabbitmq.routing-key.schedules}")
    private String routingKey;

    @Bean
    public Queue schedulesQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange schedulesExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding schedulesBinding() {
        return BindingBuilder
            .bind(schedulesQueue())
            .to(schedulesExchange())
            .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
