package com.studentmanagement.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentEvent {
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String studentEmail;
    private String studentName;
    private String courseName;
    private String status;
    private LocalDateTime eventTimestamp;
}
