package com.class_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;

import com.class_management.entity.Course;
import com.class_management.entity.Enrollment;
import com.class_management.entity.Role;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.service.CourseService;
import com.class_management.service.EnrollmentService;
import com.class_management.service.StudentService;
import com.class_management.service.TeacherService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Add this annotation
public class AdminController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("")
    public String adminHome(Model model, @AuthenticationPrincipal Teacher teacher) {
        if (teacher.getRole() != Role.ADMIN) {
            return "redirect:/home";
        }
        List<Teacher> teachers = teacherService.getAllTeachers();
        List<Student> students = studentService.getAllStudents();
        List<Course> courses = courseService.getAllCourse();

        // Map teacher name to student count
        Map<String, Long> studentsPerTeacher = teachers.stream()
                .collect(Collectors.toMap(
                        Teacher::getName,
                        t -> studentService.getStudentsByTeacher(t).stream().count(),
                        Long::sum));

        // Map teacher name to course count
        Map<String, Long> coursesPerTeacher = teachers.stream()
                .collect(Collectors.toMap(
                        Teacher::getName,
                        t -> courseService.getCoursesByTeacher(t).stream().count(),
                        Long::sum));

        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);
        model.addAttribute("courses", courses);
        model.addAttribute("teacher", teacher);
        model.addAttribute("studentsPerTeacher", studentsPerTeacher);
        model.addAttribute("coursesPerTeacher", coursesPerTeacher);
        model.addAttribute("pageName", "Admin Dashboard");
        return "admin_index";
    }

    @GetMapping("/users")
    public String teachers(Model model, @AuthenticationPrincipal Teacher teacher) {
        if (teacher.getRole() != Role.ADMIN) {
            return "redirect:/home";
        }
        List<Teacher> users = teacherService.getAllTeachers();

        model.addAttribute("users", users);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pageName", "Teachers List");
        return "admin_user";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam Role newRole, RedirectAttributes ra) {
        try {
            teacherService.updateTeacherRole(id, newRole);
            ra.addFlashAttribute("message", "User role updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update user role");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/disable/{id}")
    public String disableUser(RedirectAttributes ra, @PathVariable Long id) {
        teacherService.setDisable(id);

        // Reload to get fresh value from DB
        Teacher teacher = teacherService.getTeacherById(id);
        System.out.println("Disabled user with ID: " + id + ", Enabled = " + teacher.isEnabled());

        ra.addFlashAttribute("message", "User has been disabled");
        return "redirect:/admin/users";
    }

    @PostMapping("/enable/{id}")
    public String enableUser(RedirectAttributes ra, @PathVariable Long id) {
        teacherService.setEnable(id);

        // Reload to get fresh value from DB
        Teacher teacher = teacherService.getTeacherById(id);
        System.out.println("Enabled user with ID: " + id + ", Enabled = " + teacher.isEnabled());

        ra.addFlashAttribute("message", "User has been enabled");
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String showCourses(@RequestParam(required = false) Long selectedUserId,
            @RequestParam(required = false) String selectedCourseId, Model model,
            @AuthenticationPrincipal Teacher teacher) {

        if (teacher.getRole() != Role.ADMIN) {
            return "redirect:/home";
        }

        List<Teacher> teachers = teacherService.getAllTeachers();

        List<Course> courses;
        if (selectedUserId != null) {
            courses = courseService.getCoursesByTeacher(teacherService.getTeacherById(selectedUserId));
        } else {
            courses = courseService.getAllCourse();
        }

        Course course = null;
        List<Student> students = new ArrayList<>();

        if (selectedCourseId != null) {
            course = courseService.getCourseById(selectedCourseId);
            students = course.getEnrollments()
                    .stream()
                    .map(Enrollment::getStudent)
                    .collect(Collectors.toList());
            model.addAttribute("selectedCourseId", selectedCourseId);
            model.addAttribute("students", students);
        }

        model.addAttribute("students", students);
        model.addAttribute("selectedCourseId", selectedCourseId);
        model.addAttribute("selectedUserId", selectedUserId);
        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pageName", "Courses List");
        return "admin_course";
    }

    @GetMapping("/students")
    public String showStudents(@RequestParam(required = false) Long selectedUserId, Model model,
            @AuthenticationPrincipal Teacher teacher) {
        if (teacher.getRole() != Role.ADMIN) {
            return "redirect:/home";
        }
        List<Teacher> teachers = teacherService.getAllTeachers();
        List<Student> students;
        if (selectedUserId != null) {
            students = studentService.getStudentsByTeacher(teacherService.getTeacherById(selectedUserId));
            if (students == null) {
                model.addAttribute("error", "Student not found");
                return "admin_student";
            }
            model.addAttribute("selectedUserId", selectedUserId);
        } else {
            students = studentService.getAllStudents();
            model.addAttribute("selectedUserId", null);
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("selectedUserId", selectedUserId);
        model.addAttribute("students", students);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pageName", "Students List");
        return "admin_student";
    }

}