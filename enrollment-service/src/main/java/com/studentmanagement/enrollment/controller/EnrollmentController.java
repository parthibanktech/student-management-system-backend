package com.studentmanagement.enrollment.controller;

import com.studentmanagement.enrollment.dto.EnrollmentRequest;
import com.studentmanagement.enrollment.dto.EnrollmentResponse;
import com.studentmanagement.enrollment.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==========================================================================================================
 * ENROLLMENT CONTROLLER - REST API
 * ==========================================================================================================
 * The entry point for Student Enrollments.
 * 
 * CORE RESPONSIBILITY:
 * - Orchestrates the start of the Enrollment Saga.
 * - Bridges the Gap between Frontend interactions and the Event-Driven Backend.
 * - Handles retries and manual overrides.
 */
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        logger.info("REST request to get all enrollments");
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Long id) {
        logger.info("REST request to get enrollment by id: {}", id);
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        logger.info("REST request to get enrollments for student: {}", studentId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> createEnrollment(@RequestBody EnrollmentRequest enrollment) {
        logger.info("REST request to create enrollment: {}", enrollment);
        return new ResponseEntity<>(enrollmentService.createEnrollment(enrollment), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        logger.info("REST request to delete enrollment with id: {}", id);
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirm Enrollment (Manual Override).
     * <p>
     * Used by Admins to forcefully set an enrollment to CONFIRMED.
     * Use with caution as it bypasses Sag checks.
     * </p>
     * 
     * @param id Enrollment ID.
     * @return Updated Enrollment.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(@PathVariable Long id) {
        logger.info("REST request to confirm enrollment payment: {}", id);
        return ResponseEntity.ok(enrollmentService.confirmEnrollment(id));
    }

    /**
     * Retry Enrollment Saga.
     * <p>
     * If an enrollment is stuck in PENDING due to a glitch (e.g., Payment Service
     * was down),
     * this endpoint re-publishes the 'enrollment-initiated' event to kickstart the
     * process again.
     * </p>
     * 
     * @param id Enrollment ID.
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryEnrollment(@PathVariable Long id) {
        logger.info("REST request to retry enrollment saga: {}", id);
        enrollmentService.retryEnrollment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Enrollment Service is Healthy");
    }
}
