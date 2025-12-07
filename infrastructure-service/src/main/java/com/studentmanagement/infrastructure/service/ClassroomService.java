package com.studentmanagement.infrastructure.service;

import com.studentmanagement.infrastructure.entity.Classroom;
import com.studentmanagement.infrastructure.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
    }

    public Classroom getClassroomById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + id));
    }

    public Classroom createClassroom(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    public Classroom updateClassroom(Long id, Classroom classroomDetails) {
        Classroom classroom = getClassroomById(id);
        classroom.setRoomNumber(classroomDetails.getRoomNumber());
        classroom.setBuildingName(classroomDetails.getBuildingName());
        classroom.setCapacity(classroomDetails.getCapacity());
        classroom.setRoomType(classroomDetails.getRoomType());
        return classroomRepository.save(classroom);
    }

    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }
}
