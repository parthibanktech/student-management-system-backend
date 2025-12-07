package com.studentmanagement.enrollment.service;

import com.studentmanagement.enrollment.client.CourseClient;
import com.studentmanagement.enrollment.client.StudentClient;
import com.studentmanagement.enrollment.dto.EnrollmentRequest;
import com.studentmanagement.enrollment.dto.EnrollmentResponse;
import com.studentmanagement.enrollment.entity.Enrollment;
import com.studentmanagement.enrollment.event.EnrollmentEvent;
import com.studentmanagement.enrollment.event.EnrollmentInitiatedEvent;
import com.studentmanagement.enrollment.kafka.EnrollmentProducer;
import com.studentmanagement.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enrollment Service Implementation
 * <p>
 * This service orchestrates the enrollment process using a Saga pattern.
 * It interacts with Student and Course services to validate data and uses Kafka
 * to coordinate distributed transactions (Payment, Seat Reservation).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentProducer enrollmentProducer;
    private final StudentClient studentClient;
    private final CourseClient courseClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Enroll a student in a course
     * <p>
     * Initiates the enrollment Saga.
     * 1. Validates Student and Course existence via synchronous HTTP calls.
     * 2. Creates an Enrollment record with status PENDING.
     * 3. Publishes 'enrollment-initiated' event to start the Saga.
     * </p>
     *
     * @param request Enrollment request details
     * @return EnrollmentResponse with PENDING status
     */
    @SuppressWarnings("null")
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        try {
            // Verify student exists (Synchronous call to Student Service)
            Map<String, Object> student = studentClient.getStudent(request.getStudentId());

            // Verify course exists (Synchronous call to Course Service)
            Map<String, Object> course = courseClient.getCourse(request.getCourseId());

            if (student == null || course == null) {
                throw new RuntimeException("Student or Course not found");
            }

            // Extract student email
            String studentEmail = (String) student.get("email");

            // Create enrollment record
            Enrollment enrollment = Enrollment.builder()
                    .studentId(request.getStudentId())
                    .courseId(request.getCourseId())
                    .enrollmentDate(LocalDateTime.now())
                    .status(Enrollment.EnrollmentStatus.PENDING)
                    .build();

            enrollment = enrollmentRepository.save(enrollment);
            log.info("[SAGA START] Enrollment initiated. ID: {}, StudentID: {}, CourseID: {}, Status: PENDING",
                    enrollment.getId(), request.getStudentId(), request.getCourseId());

            // Publish event to Kafka for Saga (Step 1)
            EnrollmentInitiatedEvent sagaEvent = EnrollmentInitiatedEvent.builder()
                    .enrollmentId(enrollment.getId())
                    .studentId(enrollment.getStudentId())
                    .courseId(enrollment.getCourseId())
                    .studentEmail(studentEmail) // Pass email for notifications
                    .studentName(student.get("name") != null ? student.get("name").toString() : "Unknown")
                    .courseName(course.get("title") != null ? course.get("title").toString() : "Unknown")
                    .build();

            log.info("[SAGA STEP 1] Publishing 'enrollment-initiated' event for EnrollmentID: {}", enrollment.getId());
            kafkaTemplate.send("enrollment-initiated", sagaEvent);

            return buildEnrollmentResponse(enrollment, student, course);
        } catch (Exception e) {
            log.error("Error enrolling student: {}", e.getMessage(), e);
            // Instead of throwing 500, return a CANCELLED/FAILED response
            // This prevents the frontend from crashing and allows basic error handling
            return EnrollmentResponse.builder()
                    .id(null) // ID is null because save might have failed
                    .studentId(request.getStudentId())
                    .courseId(request.getCourseId())
                    .studentName("Unknown (Service Error)")
                    .courseTitle("Unknown (Service Error)")
                    .enrollmentDate(LocalDateTime.now())
                    .status(Enrollment.EnrollmentStatus.CANCELLED)
                    .build();
        }
    }

    /**
     * Get all enrollments
     */
    public List<EnrollmentResponse> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return enrollments.stream()
                .map(this::enrichEnrollmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get enrollment by ID
     */
    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + id));
        return enrichEnrollmentResponse(enrollment);
    }

    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        return enrollStudent(request);
    }

    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new RuntimeException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(this::enrichEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream()
                .map(this::enrichEnrollmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Enrich Enrollment entity with details from other services
     */
    private EnrollmentResponse enrichEnrollmentResponse(Enrollment enrollment) {
        Map<String, Object> student = fetchStudent(enrollment.getStudentId());
        Map<String, Object> course = fetchCourse(enrollment.getCourseId());
        return buildEnrollmentResponse(enrollment, student, course);
    }

    private Map<String, Object> fetchStudent(Long studentId) {
        try {
            return studentClient.getStudent(studentId);
        } catch (Exception e) {
            log.error("Failed to fetch student {}", studentId, e);
            return Map.of("name", "Unknown");
        }
    }

    private Map<String, Object> fetchCourse(Long courseId) {
        try {
            return courseClient.getCourse(courseId);
        } catch (Exception e) {
            log.error("Failed to fetch course {}", courseId, e);
            return Map.of("title", "Unknown");
        }
    }

    private EnrollmentResponse buildEnrollmentResponse(Enrollment enrollment,
            Map<String, Object> student,
            Map<String, Object> course) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .studentName(student.get("name").toString())
                .courseId(enrollment.getCourseId())
                .courseTitle(course.get("title").toString())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus())
                .build();
    }

    /**
     * Handle Saga Success: Seat Reserved
     * <p>
     * This is the final step of the Saga. If payment and seat reservation succeed,
     * the enrollment is CONFIRMED.
     * </p>
     */
    @KafkaListener(topics = "seat-reserved", groupId = "enrollment-group")
    public void handleSeatReserved(EnrollmentInitiatedEvent event) {
        log.info("[SAGA SUCCESS] Received 'seat-reserved' event for EnrollmentID: {}. Completing enrollment.",
                event.getEnrollmentId());
        updateEnrollmentStatus(event.getEnrollmentId(), Enrollment.EnrollmentStatus.CONFIRMED);

        // Fetch details for notification
        String studentName = "Unknown";
        String courseName = "Unknown";
        try {
            Map<String, Object> student = studentClient.getStudent(event.getStudentId());
            Map<String, Object> course = courseClient.getCourse(event.getCourseId());
            if (student != null)
                studentName = (String) student.get("name");
            if (course != null)
                courseName = (String) course.get("title");
        } catch (Exception e) {
            log.warn("Could not fetch details for notification: {}", e.getMessage());
        }

        // Publish Enrollment Event for Notification Service
        EnrollmentEvent enrollmentEvent = EnrollmentEvent.builder()
                .enrollmentId(event.getEnrollmentId())
                .studentId(event.getStudentId())
                .courseId(event.getCourseId())
                .studentEmail(event.getStudentEmail())
                .studentName(studentName)
                .courseName(courseName)
                .status("ACTIVE")
                .eventTimestamp(LocalDateTime.now())
                .build();

        enrollmentProducer.sendEnrollmentEvent(enrollmentEvent);
        log.info("Published EnrollmentEvent for Notification Service: {}", enrollmentEvent);
    }

    /**
     * Handle Saga Failure
     * <p>
     * If Payment or Seat Reservation fails, this listener triggers a rollback
     * by setting the status to CANCELLED.
     * </p>
     */
    @KafkaListener(topics = { "payment-failed", "seat-reservation-failed" }, groupId = "enrollment-group")
    public void handleSagaFailure(EnrollmentInitiatedEvent event) {
        log.error("[SAGA FAILURE] Received failure event for EnrollmentID: {}. Rolling back to CANCELLED.",
                event.getEnrollmentId());
        updateEnrollmentStatus(event.getEnrollmentId(), Enrollment.EnrollmentStatus.CANCELLED);
    }

    public EnrollmentResponse confirmEnrollment(Long id) {
        updateEnrollmentStatus(id, Enrollment.EnrollmentStatus.CONFIRMED);
        return getEnrollmentById(id);
    }

    private void updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status) {
        enrollmentRepository.findById(enrollmentId).ifPresentOrElse(enrollment -> {
            log.info("[MANUAL UPDATE] Updating EnrollmentID: {} status from {} to {}",
                    enrollmentId, enrollment.getStatus(), status);
            enrollment.setStatus(status);
            enrollmentRepository.save(enrollment);
        }, () -> log.error("Enrollment not found for ID: {}", enrollmentId));
    }

    public void retryEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new RuntimeException("Can only retry PENDING enrollments");
        }

        Map<String, Object> student = fetchStudent(enrollment.getStudentId());
        Map<String, Object> course = fetchCourse(enrollment.getCourseId());

        EnrollmentInitiatedEvent sagaEvent = EnrollmentInitiatedEvent.builder()
                .enrollmentId(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .studentEmail((String) student.getOrDefault("email", "unknown@example.com"))
                // Handle potential nulls safely
                .studentName(student.get("name") != null ? student.get("name").toString() : "Unknown")
                .courseName(course.get("title") != null ? course.get("title").toString() : "Unknown")
                .build();

        log.info("[RETRY] Re-publishing 'enrollment-initiated' event for EnrollmentID: {}", enrollment.getId());
        kafkaTemplate.send("enrollment-initiated", sagaEvent);
    }
}
