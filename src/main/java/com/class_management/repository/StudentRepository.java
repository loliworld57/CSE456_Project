package com.class_management.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    List<Student> findByTeacher(Teacher teacher);

    List<Student> findByStudentNameContainingIgnoreCase(String keyword);

    List<Student> findByStudentNameContainingIgnoreCaseAndTeacher(String keyword, Teacher teacher);

    Page<Student> findByTeacher(Teacher teacher, Pageable pageable);

    Page<Student> findByStudentNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Student> findByStudentNameContainingIgnoreCaseAndTeacher(String keyword, Teacher teacher, Pageable pageable);
    Page<Student> findAll(Pageable pageable);
}
