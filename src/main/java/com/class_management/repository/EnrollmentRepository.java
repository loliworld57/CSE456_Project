package com.class_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.class_management.entity.Enrollment;
import com.class_management.entity.Student;
import com.class_management.entity.Course;
import java.util.*;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findStudentById(Integer studentId);
    List<Enrollment> findCourseById(Integer courseId);
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    
    @Query("SELECT e.student FROM Enrollment e WHERE e.course.id = :courseId")
    Page<Student> findStudentsByCourseId(@Param("courseId") String courseId, Pageable pageable);

    @Query("SELECT e.finalScore FROM Enrollment e WHERE e.student.teacher.teacherId = :teacherId")
    List<Float> findAllScoresByTeacherId(@Param("teacherId") Long teacherId);
    
}
