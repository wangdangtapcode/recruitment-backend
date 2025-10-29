package com.example.candidate_service.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventsProducer {

    public static final String TOPIC_APPLICATION_SUBMITTED = "application-submitted";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ApplicationEventsProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishApplicationSubmitted(Long jobPositionId) {
        if (jobPositionId == null)
            return;
        kafkaTemplate.send(TOPIC_APPLICATION_SUBMITTED, String.valueOf(jobPositionId));
    }
}
