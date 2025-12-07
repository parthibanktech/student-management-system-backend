package com.studentmanagement.notification.service;

import com.studentmanagement.notification.event.EnrollmentEvent;
import com.studentmanagement.notification.event.StudentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notification Listener Service
 * <p>
 * This service listens to Kafka topics for events from other microservices
 * (Student Service, Enrollment Service) and processes them to send
 * notifications.
 * </p>
 * <p>
 * This implementation uses JavaMailSender to send actual emails.
 * Ensure that the mail properties (host, port, username, password) are
 * correctly
 * configured in application.yml or via environment variables.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final JavaMailSender mailSender;

    /**
     * Kafka Listener for Student Events
     * <p>
     * Listens to the 'student-events' topic.
     * When a student is created, updated, or deleted, this method is triggered.
     * </p>
     *
     * @param event The StudentEvent object received from Kafka
     */
    @KafkaListener(topics = "${spring.kafka.topic.student-events}", groupId = "notification-group")
    public void handleStudentEvent(StudentEvent event) {
        log.info("Received Kafka Event: {}", event);

        if ("CREATED".equals(event.getEventType())) {
            sendWelcomeEmail(event.getStudentEmail());
        }
    }

    /**
     * Kafka Listener for Enrollment Events
     * <p>
     * Listens to the 'enrollment-events' topic.
     * When a student enrolls in a course, this method is triggered.
     * </p>
     *
     * @param event The EnrollmentEvent object received from Kafka
     */
    @KafkaListener(topics = "${spring.kafka.topic.enrollment-events}", groupId = "notification-group")
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        log.info("Received Enrollment Event: {}", event);

        if ("ACTIVE".equals(event.getStatus())) {
            sendEnrollmentConfirmation(event);
        }
    }

    /**
     * Send a welcome email
     *
     * @param email The recipient's email address
     */
    private void sendWelcomeEmail(String email) {
        log.info("📧 Preparing to send welcome email to {}", email);

        if (email == null || email.isEmpty()) {
            log.warn("⚠️ Cannot send welcome email: Email is missing.");
            return;
        }

        String subject = "Welcome to Student Management System";
        String body = "Welcome! You have successfully registered.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Welcome email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}", email, e);
        }
    }

    /**
     * Send an enrollment confirmation email
     *
     * @param event The enrollment event containing details
     */
    private void sendEnrollmentConfirmation(EnrollmentEvent event) {
        String email = event.getStudentEmail();
        String studentName = event.getStudentName() != null ? event.getStudentName() : "Student";
        String courseName = event.getCourseName() != null ? event.getCourseName() : "Course ID " + event.getCourseId();

        log.info("📧 Preparing to send enrollment confirmation for {} in {} to {}", studentName, courseName, email);

        if (email == null || email.isEmpty()) {
            log.warn("⚠️ Cannot send enrollment email: Student email is missing.");
            return;
        }

        String subject = "Enrollment Confirmation: " + courseName;
        String body = "Dear " + studentName + ",\n\n" +
                "You have been successfully enrolled in " + courseName + ".\n" +
                "Enrollment ID: " + event.getEnrollmentId() + "\n\n" +
                "Happy Learning!";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Enrollment confirmation email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send enrollment confirmation email to {}", email, e);
        }
    }
}
