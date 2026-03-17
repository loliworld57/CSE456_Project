package com.class_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.class_management.entity.Course;
import com.class_management.entity.Enrollment;
import com.class_management.entity.Role;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.exception.StudentNotFoundException;
import com.class_management.repository.StudentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {
    @Autowired
    private StudentRepository stuR;

    @Autowired
    private CourseService couService;

    public List<Student> getAllStudents() {
        return (List<Student>) stuR.findAll();
    }

    public void save(Student stu) {
        stuR.save(stu);

    }

    public List<Student> getStudentsByTeacher(Teacher teacher) {
        return stuR.findByTeacher(teacher);
    }

    public Student getStudent(Long id) throws StudentNotFoundException {
        Optional<Student> stu = stuR.findById(id);
        if (stu.isPresent())
            return stu.get();

        throw new StudentNotFoundException("Could not find this student");
    }

    public void deleteStudent(Long id) throws StudentNotFoundException {

        if (stuR.findById(id).isPresent()) {
            stuR.deleteById(id);
        } else {
            throw new StudentNotFoundException("Could not find this student " + id);
        }
    }

    public List<Student> getStudentsNotInCourse(String courseId, Teacher teacher)
            throws StudentNotFoundException, CourseNotFoundException {
        List<Student> allStudents = getStudentsByTeacher(teacher);
        Course course = couService.getCourse(courseId);
        Set<Student> enrolledStudents = course.getEnrollments()
                .stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toSet());

        return allStudents.stream()
                .filter(student -> !enrolledStudents.contains(student))
                .collect(Collectors.toList());
    }

    public List<Student> searchStudents(String keyword, Teacher teacher) {
        if (keyword == null || keyword.isEmpty()) {
            return getStudentsByTeacher(teacher);
        }
        return teacher.getRole() == Role.ADMIN ? stuR.findByStudentNameContainingIgnoreCase(keyword)
                : stuR.findByStudentNameContainingIgnoreCaseAndTeacher(keyword, teacher);
    }
    public Page<Student> searchStudents(String keyword, Teacher teacher, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return teacher.getRole() == Role.ADMIN ? 
                   stuR.findAll(pageable) : 
                   stuR.findByTeacher(teacher, pageable);
        }
        return teacher.getRole() == Role.ADMIN ? 
               stuR.findByStudentNameContainingIgnoreCase(keyword, pageable) :
               stuR.findByStudentNameContainingIgnoreCaseAndTeacher(keyword, teacher, pageable);
    }
}
