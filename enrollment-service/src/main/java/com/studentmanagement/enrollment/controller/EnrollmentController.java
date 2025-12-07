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

    @PostMapping("/{id}/confirm")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(@PathVariable Long id) {
        logger.info("REST request to confirm enrollment payment: {}", id);
        return ResponseEntity.ok(enrollmentService.confirmEnrollment(id));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryEnrollment(@PathVariable Long id) {
        logger.info("REST request to retry enrollment saga: {}", id);
        enrollmentService.retryEnrollment(id);
        return ResponseEntity.ok().build();
    }
}
