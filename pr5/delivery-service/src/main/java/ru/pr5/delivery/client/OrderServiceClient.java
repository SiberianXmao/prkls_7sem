package ru.pr5.delivery.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.pr5.delivery.service.RelatedOrderNotFoundException;
import ru.pr5.delivery.service.RemoteOrderServiceException;

@Component
public class OrderServiceClient {
    private final RestClient restClient;

    public OrderServiceClient(RestClient orderRestClient) {
        this.restClient = orderRestClient;
    }

    public OrderSnapshot findById(long orderId) {
        try {
            OrderSnapshot order = restClient.get()
                    .uri("/api/orders/{orderId}", orderId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new RelatedOrderNotFoundException(orderId);
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RemoteOrderServiceException(
                                "Order Service вернул HTTP " + response.getStatusCode().value());
                    })
                    .body(OrderSnapshot.class);
            if (order == null) {
                throw new RemoteOrderServiceException("Order Service вернул пустой ответ");
            }
            return order;
        } catch (RelatedOrderNotFoundException | RemoteOrderServiceException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new RemoteOrderServiceException("Order Service недоступен", exception);
        }
    }
}
