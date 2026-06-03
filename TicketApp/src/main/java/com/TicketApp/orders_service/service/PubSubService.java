package com.TicketApp.orders_service.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.TicketApp.orders_service.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PubSubService {
    @Value("${gcp.project-id}")
    private String projectId;
    
    @Value("${gcp.pubsub.topic-id}")
    private String topicId;

    public void publishOrderCreated(Order order) {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topicName).build();

            // Solo enviamos el ID
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getId().toString());

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

    public String enviarMensaje(String mensaje) {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topicName).build();

            ByteString data = ByteString.copyFromUtf8(mensaje);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .build();

            String messageId = publisher.publish(pubsubMessage).get();
            return "Mensaje enviado con ID: " + messageId;

        } catch (Exception e) {
            throw new RuntimeException("Error enviando mensaje a Pub/Sub: " + e.getMessage(), e);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
