package com.TicketApp.orders_service.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class PubSubService {
    private final String PROJECT_ID = "Sistemas-Distribuidos-II-2026";
    
    private final String TOPIC_ID = "orders-service-fran";

    public String enviarMensaje(String textoMensaje) {
        TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_ID);
        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topicName).build();
            
            ByteString data = ByteString.copyFromUtf8(textoMensaje);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .build();
            
            String messageId = publisher.publish(pubsubMessage).get();
            return "Éxito. Mensaje publicado en Pub/Sub con ID: " + messageId;
        } catch (InterruptedException | ExecutionException | java.io.IOException e) {
            throw new RuntimeException("Error publicando en la Queue de GCP: " + e.getMessage(), e);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
