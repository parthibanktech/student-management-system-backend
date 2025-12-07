package com.studentmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentEvent {
    private String eventType; // CREATED, UPDATED, DELETED
    private Long studentId;
    private String studentEmail;
    private String timestamp;

}
