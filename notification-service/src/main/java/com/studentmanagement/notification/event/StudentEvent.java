package com.studentmanagement.notification.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentEvent {
    private String eventType;
    private Long studentId;
    private String studentEmail;
    private String timestamp;
}
