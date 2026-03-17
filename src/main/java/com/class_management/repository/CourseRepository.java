package com.class_management.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.class_management.entity.Course;
import com.class_management.entity.Teacher;

@Repository
public interface CourseRepository extends CrudRepository<Course, String> {
    
    List<Course> findByTeacher(Teacher teacher);

    List<Course> findByCourseNameContainingIgnoreCase(String keyword);

    List<Course> findByCourseNameContainingIgnoreCaseAndTeacher(String keyword, Teacher teacher);

    Page<Course> findByTeacher(Teacher teacher, Pageable pageable);

    Page<Course> findByCourseNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Course> findByCourseNameContainingIgnoreCaseAndTeacher(String keyword, Teacher teacher, Pageable pageable);

    Page<Course> findAll(Pageable pageable);

    Course findByCourseId(String courseId);

}
