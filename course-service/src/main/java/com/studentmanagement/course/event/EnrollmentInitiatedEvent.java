package com.studentmanagement.course.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentInitiatedEvent {
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String studentEmail;
    private String studentName;
    private String courseName;
}
