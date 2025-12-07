package com.studentmanagement.repository;

import com.studentmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Student Repository Interface
 * Provides data access methods for Student entity
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);

    /**
     * Search students by name (partial match)
     */
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> searchByName(@Param("name") String name);

    /**
     * Find students by grade
     */
    List<Student> findByGrade(String grade);

    /**
     * Find students by city
     */
    List<Student> findByCity(String city);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Search students by multiple fields
     */
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Student> searchStudents(@Param("searchTerm") String searchTerm);
}
