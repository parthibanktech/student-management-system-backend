package com.studentmanagement.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String studentName;
    private String courseName;
    private BigDecimal amount;
    private String status; // PENDING, PAID, FAILED, REFUNDED
    private LocalDateTime paymentDate;
}
