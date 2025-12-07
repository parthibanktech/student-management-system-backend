package com.studentmanagement.course.service;

import com.studentmanagement.course.dto.CourseDTO;
import com.studentmanagement.course.entity.Course;
import com.studentmanagement.course.exception.ResourceNotFoundException;
import com.studentmanagement.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import com.studentmanagement.course.event.EnrollmentInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Course Service Implementation
 * <p>
 * This service manages the lifecycle of Course entities, including creation,
 * retrieval, updates, and deletion. It also handles seat reservations as part
 * of the enrollment Saga pattern.
 * </p>
 */
@Service // Marks this class as a Spring Service
@RequiredArgsConstructor // Generates constructor for final fields
@Slf4j // Enables logging
public class CourseService {

    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create a new course
     *
     * @param courseDTO Data for the new course
     * @return The created CourseDTO
     */
    @SuppressWarnings("null")
    public CourseDTO createCourse(CourseDTO courseDTO) {
        // Convert DTO to Entity
        Course course = convertToEntity(courseDTO);
        // Save to database
        Course savedCourse = courseRepository.save(course);
        // Return DTO
        return convertToDTO(savedCourse);
    }

    /**
     * Get all courses
     *
     * @return List of all courses
     */
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     *
     * @param id Course ID
     * @return CourseDTO
     */
    public CourseDTO getCourseById(@NonNull Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return convertToDTO(course);
    }

    /**
     * Update an existing course
     *
     * @param id        Course ID
     * @param courseDTO Updated data
     * @return Updated CourseDTO
     */
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Update fields
        existingCourse.setTitle(courseDTO.getTitle());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setCredits(courseDTO.getCredits());
        if (courseDTO.getCapacity() != null) {
            existingCourse.setCapacity(courseDTO.getCapacity());
        }

        Course updatedCourse = courseRepository.save(existingCourse);
        return convertToDTO(updatedCourse);
    }

    /**
     * Delete a course
     *
     * @param id Course ID
     */
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    /**
     * Convert Entity to DTO
     */
    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .credits(course.getCredits())
                .capacity(course.getCapacity())
                .enrolledCount(course.getEnrolledCount())
                .build();
    }

    /**
     * Convert DTO to Entity
     */
    private Course convertToEntity(CourseDTO courseDTO) {
        return Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .credits(courseDTO.getCredits())
                .capacity(courseDTO.getCapacity() != null ? courseDTO.getCapacity() : 50) // Default capacity 50
                .enrolledCount(0)
                .build();
    }

    /**
     * Handle Payment Success Event (Saga Step 3)
     * <p>
     * Listens for 'payment-success' events. Checks if there are available seats.
     * If yes, reserves a seat and emits 'seat-reserved'.
     * If no, emits 'seat-reservation-failed'.
     * </p>
     *
     * @param event The enrollment initiated event carrying details
     */
    @KafkaListener(topics = "payment-success", groupId = "course-group")
    public void handlePaymentSuccess(EnrollmentInitiatedEvent event) {
        log.info("[SAGA STEP 3] Received 'payment-success' event for EnrollmentID: {}. Checking seat availability...",
                event.getEnrollmentId());

        try {
            // Find the course
            Course course = courseRepository.findById(event.getCourseId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Course not found with id: " + event.getCourseId()));

            log.info("[INVENTORY CHECK] Checking seats for CourseID: {}. Capacity: {}, Enrolled: {}",
                    course.getId(), course.getCapacity(), course.getEnrolledCount());

            // Check capacity
            if (course.getEnrolledCount() < course.getCapacity()) {
                // Reserve seat
                course.setEnrolledCount(course.getEnrolledCount() + 1);
                courseRepository.save(course);

                log.info("[SEAT RESERVED] Seat reserved for EnrollmentID: {}. Publishing 'seat-reserved' event.",
                        event.getEnrollmentId());
                // Continue Saga
                kafkaTemplate.send("seat-reserved", event);
            } else {
                // Fail Saga
                log.error(
                        "[SEAT UNAVAILABLE] Seat reservation failed for EnrollmentID: {}. Publishing 'seat-reservation-failed' event.",
                        event.getEnrollmentId());
                kafkaTemplate.send("seat-reservation-failed", event);
            }
        } catch (Exception e) {
            log.error("Error processing payment-success event", e);
            // Fail Saga on error
            kafkaTemplate.send("seat-reservation-failed", event);
        }
    }
}
