package com.studentmanagement.payment.controller;

import com.studentmanagement.payment.entity.Payment;
import com.studentmanagement.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==========================================================================================================
 * PAYMENT CONTROLLER - REST API
 * ==========================================================================================================
 * Manages Payment Records.
 * 
 * NOTE: Most payment processing is simulated.
 * The 'completePayment' endpoint mocks a successful transaction callback from a
 * 3rd party provider (like Stripe).
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        logger.info("REST request to get all payments");
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        logger.info("REST request to get payment by id: {}", id);
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        logger.info("REST request to create payment: {}", payment);
        return new ResponseEntity<>(paymentService.createPayment(payment), HttpStatus.CREATED);
    }

    /**
     * Complete Payment (Mock).
     * <p>
     * Simulates a user successfully entering credit card details.
     * Updates status to PAID and trigger Saga Step 3 (via Service).
     * </p>
     * 
     * @param id Payment ID.
     * @return Updated Payment.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Payment> completePayment(@PathVariable Long id) {
        logger.info("REST request to complete payment with id: {}", id);
        return ResponseEntity.ok(paymentService.completePayment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        logger.info("REST request to delete payment with id: {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is Healthy");
    }
}
