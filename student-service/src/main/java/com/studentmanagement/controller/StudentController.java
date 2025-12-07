package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Student Management
 * Provides RESTful API endpoints for student operations
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Slf4j

public class StudentController {

    private final StudentService studentService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome Student controller";
    }

    /**
     * GET /students - Get all students
     */
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        log.info("GET /students - Fetching all students");
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * POST /students - Create new student
     */

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("POST /students - Creating new student");
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @PostMapping("/addMore")
    public ResponseEntity<List<?>> addMoreStudents(@Valid @RequestBody List<StudentDTO> studDTO) {
        List<StudentDTO> studentList = studentService.addAllStudents(studDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(studentList);
    }

    /**
     * PUT /students/{id} - Update existing student
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO) {
        log.info("PUT /students/{} - Updating student", id);
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    /**
     * DELETE /students/{id} - Delete student
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        log.info("DELETE /students/{} - Deleting student", id);
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /students/search?term={searchTerm} - Search students
     */
    @GetMapping("/search")
    public ResponseEntity<List<StudentDTO>> searchStudents(@RequestParam String term) {
        log.info("GET /students/search?term={} - Searching students", term);
        List<StudentDTO> students = studentService.searchStudents(term);
        return ResponseEntity.ok(students);
    }

    /**
     * GET /students/grade/{grade} - Get students by grade
     */
    @GetMapping("/grade/{grade}")
    public ResponseEntity<List<StudentDTO>> getStudentsByGrade(@PathVariable String grade) {
        log.info("GET /students/grade/{} - Fetching students by grade", grade);
        List<StudentDTO> students = studentService.getStudentsByGrade(grade);
        return ResponseEntity.ok(students);
    }

    /**
     * GET /students/health - Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Student Service is running!");
    }

    /**
     * GET /students/{id} - Get student by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        log.info("GET /students/{} - Fetching student by id", id);
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
}
