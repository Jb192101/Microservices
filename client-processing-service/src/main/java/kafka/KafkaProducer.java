package kafka;

import kafka.dto.ClientCardMessage;
import kafka.dto.KafkaMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object payload, String eventType) {
        KafkaMessageDTO message = new KafkaMessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setEventType(eventType);
        message.setTimestamp(LocalDateTime.now());
        message.setPayload(payload);

        kafkaTemplate.send(topic, message);
    }

    public void sendClientProductEvent(Object payload, String eventType) {
        sendMessage("client_products", payload, eventType);
    }

    public void sendClientCreditProductEvent(Object payload, String eventType) {
        sendMessage("client_credit_products", payload, eventType);
    }

    public void sendClientCardEvent(ClientCardMessage payload, String eventType) {
        sendMessage("client_cards", payload, eventType);
    }
}