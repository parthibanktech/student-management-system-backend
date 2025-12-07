package com.studentmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Student Entity Model
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Contact is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be 10 digits")
    @Column(nullable = false)
    private String contact;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotNull(message = "Enrollment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @NotBlank(message = "Grade is required")
    @Pattern(regexp = "^[A-F][+-]?$", message = "Grade must be valid (A, B, C, D, F with optional + or -)")
    @Column(nullable = false)
    private String grade;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
