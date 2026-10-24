package ru.pr8.delivery.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.pr8.delivery.service.DeliveryEventHandler;

import java.time.Duration;

@Component
public class OrderCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);
    private final ObjectMapper objectMapper;
    private final DeliveryEventHandler eventHandler;

    public OrderCreatedListener(ObjectMapper objectMapper, DeliveryEventHandler eventHandler) {
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
    }

    @RabbitListener(queues = RabbitTopology.QUEUE)
    public void receive(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("requestId={} eventId={} orderId={} type=OrderCreated",
                    event.requestId(), event.eventId(), event.orderId());
            // Rabbit's listener API is imperative; waiting here lets the container acknowledge only after R2DBC completes.
            eventHandler.handle(event).block(Duration.ofSeconds(5));
        } catch (JsonProcessingException exception) {
            throw new AmqpRejectAndDontRequeueException("Invalid OrderCreated event", exception);
        }
    }
}
