package com.class_management.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.class_management.entity.Course;
import com.class_management.entity.Enrollment;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.exception.StudentNotFoundException;
import com.class_management.repository.EnrollmentRepository;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    public void enrollStudent(Long studentId, String courseId)
            throws StudentNotFoundException, CourseNotFoundException {
        Student student = studentService.getStudent(studentId);
        Course course = courseService.getCourse(courseId);
        Enrollment enrollment = new Enrollment(student, course);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void save(Enrollment enrollment) {
        enrollmentRepository.save(enrollment);
    }

    public Enrollment getEnrollment(Long studentId, String courseId)
            throws StudentNotFoundException, CourseNotFoundException {
        Student student = studentService.getStudent(studentId);
        Course course = courseService.getCourse(courseId);

        return enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
    }

    public void unenrollStudent(Long studentId, String courseId)
            throws StudentNotFoundException, CourseNotFoundException {
        Student student = studentService.getStudent(studentId);
        Course course = courseService.getCourse(courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));

        enrollmentRepository.delete(enrollment);
    }

    public List<Enrollment> findTop10ByTeacherIdOrderByFinalScoreDesc(Teacher teacher) {
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        
        return allEnrollments.stream()
            .filter(e -> e.getStudent().getTeacher() != null)
            .filter(e -> e.getStudent().getTeacher().getTeacherId().equals(teacher.getTeacherId()))
            .filter(e -> e.getFinalScore() != null)
            .sorted((e1, e2) -> Double.compare(e2.getFinalScore(), e1.getFinalScore()))
            .limit(10)
            .collect(Collectors.toList());
    }
    public Page<Student> getStudentsByCourseId(String courseId, Pageable pageable) {
        return enrollmentRepository.findStudentsByCourseId(courseId, pageable);
    }
    public List<Float> getAllScoresByTeacher(Teacher teacher) {
        return enrollmentRepository.findAllScoresByTeacherId(teacher.getTeacherId());
    }
}
