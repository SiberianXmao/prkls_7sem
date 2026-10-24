package ru.pr8.delivery.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopology {
    public static final String EXCHANGE = "orders.exchange";
    public static final String ROUTING_KEY = "order.created";
    public static final String QUEUE = "delivery.order-created";
    public static final String DEAD_LETTER_EXCHANGE = "orders.dlx";
    public static final String DEAD_LETTER_QUEUE = "delivery.order-created.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "order.created.failed";

    @Bean
    DirectExchange ordersExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue orderCreatedDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(ordersExchange).with(ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue orderCreatedDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderCreatedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }
}
