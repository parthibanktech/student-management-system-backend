package com.studentmanagement.service;

import com.studentmanagement.dto.StudentDTO;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private StudentDTO studentDTO;

    @BeforeEach
    void setUp() {
        // Set value for @Value("${spring.kafka.topic.student-events}")
        ReflectionTestUtils.setField(studentService, "topicName", "student-events");

        student = new Student();
        student.setId(1L);
        student.setName("John Doe");
        student.setEmail("john@example.com");
        student.setGrade("A");

        studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("John Doe");
        studentDTO.setEmail("john@example.com");
        studentDTO.setGrade("A");
    }

    @Test
    void getAllStudents_ShouldReturnListOfStudents() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student));

        List<StudentDTO> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(student.getName(), result.get(0).getName());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getStudentById_WhenStudentExists_ShouldReturnStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDTO result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals(student.getName(), result.getName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void getStudentById_WhenStudentDoesNotExist_ShouldThrowException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(1L));
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void createStudent_WhenEmailIsUnique_ShouldSaveStudent() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentDTO result = studentService.createStudent(studentDTO);

        assertNotNull(result);
        assertEquals(student.getEmail(), result.getEmail());
        verify(studentRepository, times(1)).save(any(Student.class));
        // Verify Kafka event published
        verify(kafkaTemplate, times(1)).send(eq("student-events"), any());
    }

    @Test
    void createStudent_WhenEmailExists_ShouldThrowException() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(studentDTO));
        verify(studentRepository, never()).save(any(Student.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void deleteStudent_WhenStudentExists_ShouldDelete() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(eq("student-events"), any());
    }

    @Test
    void deleteStudent_WhenStudentDoesNotExist_ShouldThrowException() {
        when(studentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(1L));
        verify(studentRepository, never()).deleteById(anyLong());
    }
}
