package com.studentmanagement.student.service;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.event.StudentEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Student Service Implementation
 * <p>
 * This service encapsulates the business logic for managing student records
 * within the Student Management System microservices architecture.
 * </p>
 * <p>
 * Key Responsibilities:
 * <ul>
 * <li>CRUD operations for Student entities (Create, Read, Update, Delete).</li>
 * <li>Publishing lifecycle events (CREATED, UPDATED, DELETED) to Kafka for
 * asynchronous processing by other services (e.g., Notification Service).</li>
 * <li>Handling business validation (e.g., checking for duplicate emails).</li>
 * </ul>
 * </p>
 *
 * @see com.studentmanagement.repository.StudentRepository
 * @see com.studentmanagement.event.StudentEvent
 */
@Service // Marks this class as a Spring Service component to be managed by the container
@RequiredArgsConstructor // Lombok annotation to generate a constructor for final fields (dependency
                         // injection)
@Slf4j // Lombok annotation to generate a logger field (log)
@Transactional // Ensures that methods in this class are executed within a database transaction
public class StudentService {

    // Repository for interacting with the database
    private final StudentRepository studentRepository;

    // Kafka template for sending messages to Kafka topics
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // The name of the Kafka topic to publish events to, injected from
    // application.properties/yml
    @Value("${spring.kafka.topic.student-events}")
    private String topicName;

    /**
     * Get all students
     * <p>
     * Retrieves a list of all students from the database and converts them to DTOs.
     * </p>
     *
     * @return List of StudentDTO objects
     */
    public List<StudentDTO> getAllStudents() {
        log.info("Fetching all students"); // Log the action

        // Fetch all student entities from the repository
        return studentRepository.findAll()
                .stream() // Create a stream for processing
                .map(this::convertToDTO) // Convert each Student entity to a StudentDTO
                .collect(Collectors.toList()); // Collect the results into a List
    }

    /**
     * Get student by ID
     * <p>
     * Retrieves a specific student by their unique identifier.
     * </p>
     *
     * @param id The ID of the student to retrieve
     * @return The StudentDTO object
     * @throws ResourceNotFoundException if the student is not found
     */
    public StudentDTO getStudentById(Long id) {
        log.info("Fetching student with id: {}", id); // Log the action with the ID

        // Attempt to find the student by ID
        Student student = studentRepository.findById(id)
                // If not found, throw a custom exception
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Convert the found entity to a DTO and return it
        return convertToDTO(student);
    }

    /**
     * Create new student
     * <p>
     * Validates the student data, saves it to the database, and publishes a CREATED
     * event.
     * </p>
     *
     * @param studentDTO The data for the new student
     * @return The created StudentDTO with the generated ID
     * @throws DuplicateResourceException if a student with the same email already
     *                                    exists
     */
    public StudentDTO createStudent(StudentDTO studentDTO) {
        log.info("Creating new student with email: {}", studentDTO.getEmail()); // Log the creation attempt

        // Check if a student with the provided email already exists in the database
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            // If it exists, throw an exception to prevent duplicates
            throw new DuplicateResourceException("Student already exists with email: " + studentDTO.getEmail());
        }

        // Convert the input DTO to a Student entity
        Student student = convertToEntity(studentDTO);

        // Save the entity to the database
        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with id: {}", savedStudent.getId()); // Log success

        // Publish a 'CREATED' event to Kafka so other services can react (e.g., send
        // welcome email)
        publishEvent("CREATED", savedStudent);

        // Convert the saved entity back to a DTO and return it
        return convertToDTO(savedStudent);
    }

    /**
     * Update existing student
     * <p>
     * Updates the details of an existing student.
     * </p>
     *
     * @param id         The ID of the student to update
     * @param studentDTO The new data for the student
     * @return The updated StudentDTO
     * @throws ResourceNotFoundException  if the student is not found
     * @throws DuplicateResourceException if the new email is already taken by
     *                                    another student
     */
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        log.info("Updating student with id: {}", id); // Log the update attempt

        // Retrieve the existing student from the database
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Check if the email is being changed and if the new email is already taken
        if (!existingStudent.getEmail().equals(studentDTO.getEmail()) &&
                studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
        }

        // Update the fields of the existing student entity with values from the DTO
        existingStudent.setName(studentDTO.getName());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setContact(studentDTO.getContact());
        existingStudent.setCity(studentDTO.getCity());
        existingStudent.setEnrollmentDate(studentDTO.getEnrollmentDate());
        existingStudent.setGrade(studentDTO.getGrade());

        // Save the updated entity back to the database
        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Student updated successfully with id: {}", id); // Log success

        // Publish an 'UPDATED' event to Kafka
        publishEvent("UPDATED", updatedStudent);

        // Return the updated DTO
        return convertToDTO(updatedStudent);
    }

    /**
     * Delete student by ID
     * <p>
     * Removes a student record from the database.
     * </p>
     *
     * @param id The ID of the student to delete
     * @throws ResourceNotFoundException if the student does not exist
     */
    public void deleteStudent(Long id) {
        log.info("Deleting student with id: {}", id); // Log the delete attempt

        // Check if the student exists before attempting to delete
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }

        // Delete the student from the repository
        studentRepository.deleteById(id);
        log.info("Student deleted successfully with id: {}", id); // Log success

        // Publish a 'DELETED' event to Kafka
        publishEvent("DELETED", id);
    }

    /**
     * Search students
     * <p>
     * Finds students based on a search term (e.g., name or email).
     * </p>
     *
     * @param searchTerm The string to search for
     * @return List of matching StudentDTOs
     */
    public List<StudentDTO> searchStudents(String searchTerm) {
        log.info("Searching students with term: {}", searchTerm); // Log the search

        // Use the repository's custom search method
        return studentRepository.searchStudents(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get students by grade
     * <p>
     * Filters students by their grade level.
     * </p>
     *
     * @param grade The grade to filter by
     * @return List of StudentDTOs in that grade
     */
    public List<StudentDTO> getStudentsByGrade(String grade) {
        log.info("Fetching students with grade: {}", grade); // Log the filter action

        // Find students by grade using the repository
        return studentRepository.findByGrade(grade)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Entity to DTO
     * <p>
     * Helper method to map Student entity fields to StudentDTO fields.
     * </p>
     *
     * @param student The Student entity
     * @return The corresponding StudentDTO
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setContact(student.getContact());
        dto.setCity(student.getCity());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setGrade(student.getGrade());
        return dto;
    }

    /**
     * Convert DTO to Entity
     * <p>
     * Helper method to map StudentDTO fields to Student entity fields.
     * </p>
     *
     * @param dto The StudentDTO
     * @return The corresponding Student entity
     */
    private Student convertToEntity(StudentDTO dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setContact(dto.getContact());
        student.setCity(dto.getCity());
        student.setEnrollmentDate(dto.getEnrollmentDate());
        student.setGrade(dto.getGrade());
        return student;
    }

    /**
     * Publish Kafka Event (for Student entity)
     * <p>
     * Creates a StudentEvent and sends it to the configured Kafka topic.
     * </p>
     *
     * @param eventType The type of event (CREATED, UPDATED)
     * @param student   The student entity involved in the event
     */
    private void publishEvent(String eventType, Student student) {
        try {
            // Create the event object
            StudentEvent event = new StudentEvent(
                    eventType,
                    student.getId(),
                    student.getEmail(),
                    LocalDateTime.now().toString());

            // Send the event to the Kafka topic
            kafkaTemplate.send(topicName, event);
            log.info("Published Kafka event: {} for studentId: {}", eventType, student.getId());
        } catch (Exception e) {
            // Log any errors that occur during publishing, but don't fail the transaction
            log.error("Error publishing Kafka event", e);
        }
    }

    private void publishEvent(String eventType, Long studentId) {
        try {
            // Create the event object with null email
            StudentEvent event = new StudentEvent(
                    eventType,
                    studentId,
                    null,
                    LocalDateTime.now().toString());

            // Send the event to the Kafka topic
            kafkaTemplate.send(topicName, event);
            log.info("Published Kafka event: {} for studentId: {}", eventType, studentId);
        } catch (Exception e) {
            log.error("Error publishing Kafka event", e);
        }
    }

    public List<StudentDTO> addAllStudents(List<StudentDTO> studDTO) {

        // Convert all DTOs to entities
        List<Student> entities = studDTO.stream()
                .map(this::convertToEntity)
                .toList();

        List<Student> s = studentRepository.saveAll(entities);

        s.forEach(s1 -> publishEvent("CREATED", s1));

        return s.stream().map(this::convertToDTO).toList();

    }
}
