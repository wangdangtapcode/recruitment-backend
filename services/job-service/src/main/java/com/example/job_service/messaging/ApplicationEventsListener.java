package com.example.job_service.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.job_service.exception.IdInvalidException;
import com.example.job_service.service.JobPositionService;

@Component
public class ApplicationEventsListener {

    private final JobPositionService jobPositionService;

    public ApplicationEventsListener(JobPositionService jobPositionService) {
        this.jobPositionService = jobPositionService;
    }

    @KafkaListener(topics = "application-submitted", groupId = "job-service")
    public void handleApplicationSubmitted(String jobPositionIdStr) {
        try {
            Long jobPositionId = Long.valueOf(jobPositionIdStr);
            jobPositionService.incrementApplicationCount(jobPositionId);
        } catch (NumberFormatException ignored) {
            // ignore invalid payloads
        } catch (IdInvalidException ignored) {
            // job position not found; ignore
        }
    }
}
