package ru.pr8.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.pr8.order.domain.CustomerOrder;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(CustomerOrder order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getTotalPrice(),
                order.getRequestId(),
                Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.ROUTING_KEY, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize OrderCreated event", exception);
        }
    }
}
