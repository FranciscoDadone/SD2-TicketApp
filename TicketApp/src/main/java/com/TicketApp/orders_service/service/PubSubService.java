package com.TicketApp.orders_service.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.stereotype.Service;
import com.TicketApp.orders_service.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PubSubService {
    private final String PROJECT_ID = "Sistemas-Distribuidos-II-2026";
    private final String TOPIC_ID = "orders-topic";

    public void publishOrderCreated(Order order) {
        TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_ID);
        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topicName).build();
//enviar solo id por ser asincrono
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getId());
            payload.put("orderUuid", order.getOrderUuid());
            payload.put("eventId", order.getEventId());
            payload.put("eventName", order.getEventName());
            payload.put("buyerEmail", order.getBuyerEmail());
            payload.put("buyerName", order.getBuyerName());
            payload.put("quantity", order.getQuantity());
            payload.put("totalAmount", order.getTotalAmount());

            String json = new ObjectMapper().writeValueAsString(payload);

            ByteString data = ByteString.copyFromUtf8(json);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .build();

            publisher.publish(pubsubMessage).get();

        } catch (Exception e) {
            throw new RuntimeException("Error publicando en Pub/Sub: " + e.getMessage(), e);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
