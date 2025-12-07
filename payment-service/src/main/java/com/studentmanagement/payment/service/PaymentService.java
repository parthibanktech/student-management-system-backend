package com.studentmanagement.payment.service;

import com.studentmanagement.payment.entity.Payment;
import com.studentmanagement.payment.event.EnrollmentInitiatedEvent;
import com.studentmanagement.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Service Implementation
 * <p>
 * This service handles payment processing for enrollments.
 * It participates in the distributed Saga transaction by listening to
 * 'enrollment-initiated' events and publishing 'payment-success' or
 * 'payment-failed' events.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payment by ID
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    /**
     * Create a new payment record
     */
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Delete a payment record
     */
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    /**
     * Process Payment (Saga Step 2)
     * <p>
     * Listens for 'enrollment-initiated' events.
     * Simulates a payment gateway call (e.g., Stripe, PayPal).
     * If successful, records the payment and emits 'payment-success'.
     * </p>
     *
     * @param event The enrollment initiated event
     */
    @KafkaListener(topics = "enrollment-initiated", groupId = "payment-group")
    public void processPayment(EnrollmentInitiatedEvent event) {
        log.info("=========================================================================================");
        log.info("[DEBUG] Payment Service RECEIVED 'enrollment-initiated' EVENT!");
        log.info("[DEBUG] Payload: EnrollmentID={}, StudentID={}, CourseID={}",
                event.getEnrollmentId(), event.getStudentId(), event.getCourseId());
        log.info("=========================================================================================");

        log.info(
                "[SAGA STEP 2] Received 'enrollment-initiated' event for EnrollmentID: {}. Creating PENDING payment...",
                event.getEnrollmentId());

        BigDecimal amount = new BigDecimal("100.00"); // Standard course fee
        log.info("[PAYMENT CREATION] Creating payment record of ${} for StudentID: {}", amount, event.getStudentId());

        Payment payment = Payment.builder()
                .enrollmentId(event.getEnrollmentId())
                .studentId(event.getStudentId())
                .courseId(event.getCourseId())
                .studentName(event.getStudentName())
                .courseName(event.getCourseName())
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .status("PENDING") // User must manually pay
                .build();

        paymentRepository.save(payment);
        log.info("[PAYMENT PENDING] Payment created with status PENDING for EnrollmentID: {}. Waiting for user action.",
                event.getEnrollmentId());
    }

    /**
     * Complete a payment manually
     */
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for id: " + paymentId));

        if ("PAID".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is already completed.");
        }

        payment.setStatus("PAID");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info(
                "[PAYMENT COMPLETED] Manual payment successful for EnrollmentID: {}. Publishing 'payment-success' event.",
                payment.getEnrollmentId());

        // Reconstruct event to continue Saga
        EnrollmentInitiatedEvent event = new EnrollmentInitiatedEvent();
        event.setEnrollmentId(payment.getEnrollmentId());
        event.setStudentId(payment.getStudentId());
        event.setCourseId(payment.getCourseId());

        kafkaTemplate.send("payment-success", event);

        return payment;
    }

    /**
     * Handle Seat Reservation Failure (Saga Rollback)
     * <p>
     * If the Course Service fails to reserve a seat (e.g., course full),
     * this listener triggers a refund for the previously successful payment.
     * </p>
     *
     * @param event The enrollment initiated event
     */
    @KafkaListener(topics = "seat-reservation-failed", groupId = "payment-group")
    public void handleSeatReservationFailure(EnrollmentInitiatedEvent event) {
        log.warn("[SAGA ROLLBACK] Received 'seat-reservation-failed' for EnrollmentID: {}. Initiating refund...",
                event.getEnrollmentId());

        paymentRepository.findByEnrollmentId(event.getEnrollmentId()).ifPresentOrElse(payment -> {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);
            log.info("[REFUND COMPLETE] Payment refunded for EnrollmentID: {}", event.getEnrollmentId());
        }, () -> log.error("[REFUND ERROR] Payment record not found for EnrollmentID: {}", event.getEnrollmentId()));
    }
}
