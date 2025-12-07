package com.studentmanagement.enrollment.kafka;

import com.studentmanagement.enrollment.event.EnrollmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentProducer {

    private final KafkaTemplate<String, EnrollmentEvent> kafkaTemplate;
    private static final String TOPIC = "enrollment-events";

    public void sendEnrollmentEvent(EnrollmentEvent event) {
        log.info("Sending enrollment event to topic {}: {}", TOPIC, event);
        kafkaTemplate.send(TOPIC, event);
    }
}
