package com.class_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.class_management.entity.Course;
import com.class_management.entity.Role;
import com.class_management.entity.Teacher;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository couR;

    public List<Course> getAllCourse() {
        return (List<Course>) couR.findAll();
    }

    public void save(Course cou) {
        couR.save(cou);
    }

    public Course getCourse(String id) throws CourseNotFoundException {
        Optional<Course> cou = couR.findById(id);
        if (cou.isPresent())
            return cou.get();

        throw new CourseNotFoundException("Could not find this course");
    }

    public void deleteCourse(String id) throws CourseNotFoundException {

        if (couR.findById(id).isPresent()) {
            couR.deleteById(id);
        } else {
            throw new CourseNotFoundException("Could not find course " + id + " to delete");
        }
    }

    public List<Course> getCoursesByTeacher(Teacher teacher) {
        return couR.findByTeacher(teacher);
    }

    public List<Course> searchCourses(String keyword, Teacher teacher) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return teacher.getRole() == Role.ADMIN ? getAllCourse() : getCoursesByTeacher(teacher);
        }

        return teacher.getRole() == Role.ADMIN ? couR.findByCourseNameContainingIgnoreCase(keyword.trim())
                : couR.findByCourseNameContainingIgnoreCaseAndTeacher(keyword.trim(), teacher);
    }

    public Page<Course> searchCourses(String keyword, Teacher teacher, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return teacher.getRole() == Role.ADMIN ? couR.findAll(pageable) : couR.findByTeacher(teacher, pageable);
        }

        return teacher.getRole() == Role.ADMIN ? couR.findByCourseNameContainingIgnoreCase(keyword.trim(), pageable)
                : couR.findByCourseNameContainingIgnoreCaseAndTeacher(keyword.trim(), teacher, pageable);
    }

    public Course getCourseById(String id) {
        Course course = couR.findByCourseId(id);
        return course;
    }
}