package com.studentmanagement.enrollment.repository;

import com.studentmanagement.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    @Override
    @NonNull
    <S extends Enrollment> S save(@NonNull S entity);
}
