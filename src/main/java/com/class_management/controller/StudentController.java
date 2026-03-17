package com.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.class_management.entity.Enrollment;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.exception.StudentNotFoundException;
import com.class_management.service.EnrollmentService;
import com.class_management.service.StudentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class StudentController {
    @Autowired
    private StudentService stuService;
    @Autowired
    private EnrollmentService enrollmentService;

    private static final String PAGE_NAME = "Student Page";

    @GetMapping("/students")
    public String showStudents(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal Teacher teacher,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        final int PAGE_SIZE = 10;
        Page<Student> studentPage = stuService.searchStudents(keyword, teacher, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("teacher", teacher);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageName", PAGE_NAME);
        return "students";
    }

    @GetMapping("/studentlist")
    public String showStudentList(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal Teacher teacher,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        final int PAGE_SIZE = 8;
        Page<Student> studentPage = stuService.searchStudents(keyword, teacher, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("teacher", teacher);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageName", PAGE_NAME);
        return "studentlist";
    }

    @GetMapping("/students/new")
    public String showNewStudent(@AuthenticationPrincipal Teacher currentTeacher,
            Model model,
            RedirectAttributes ra) {
        Student student = new Student();
        student.setTeacher(currentTeacher);
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "Add Student");
        model.addAttribute("teacher", currentTeacher);
        model.addAttribute("pageName", PAGE_NAME);
        return "student_form";
    }

    @PostMapping("/students/save")
    public String saveStudent(
            @AuthenticationPrincipal Teacher currentTeacher,
            Student student,
            RedirectAttributes ra) {
        student.setTeacher(currentTeacher);
        stuService.save(student);
        ra.addFlashAttribute("message", "Student successfully saved");
        return "redirect:/students";
    }

    @GetMapping("/students/update/{id}")
    public String showEditStudent(@PathVariable("id") Long id, Model model, RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            Student stu = stuService.getStudent(id);
            rA.addFlashAttribute("message", "Successfully Edited");
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("student", stu);
            model.addAttribute("pageName", PAGE_NAME);
            return "student_form";
        } catch (StudentNotFoundException e) {
            rA.addFlashAttribute("error", "Failed to Edit Student");
            return "redirect:/students";
        }
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes rA) {
        try {
            if (!stuService.getStudent(id).getEnrollments().isEmpty()) {
                rA.addFlashAttribute("error",
                        "Cannot delete student " + id + ". Please remove all enrolled courses first.");
                return "redirect:/students";
            }

            rA.addFlashAttribute("message", "Sucessfully deleted student with ID: " + id);
            stuService.deleteStudent(id);
        } catch (StudentNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/students";
    }

    @GetMapping("/students/{id}/courses")
    public String showStudentCourses(@PathVariable("id") Long id,
            Model model,
            RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            Student student = stuService.getStudent(id);
            Set<Enrollment> enrollments = student.getEnrollments();
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("student", student);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("pageTitle", "Courses for " + student.getStudentName());
            model.addAttribute("pageName", PAGE_NAME);
            return "student_courses";
        } catch (StudentNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
            return "redirect:/students";
        }
    }

    @PostMapping("/students/{studentId}/courses/{courseId}/remove")
    public String removeStudentFromCourse(@PathVariable("courseId") String courseId,
            @PathVariable("studentId") Long studentId,
            RedirectAttributes rA) throws StudentNotFoundException, CourseNotFoundException {
        try {
            enrollmentService.unenrollStudent(studentId, courseId);
            rA.addFlashAttribute("message", "Student has been removed from the course");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error removing student: " + e.getMessage());
        }
        return "redirect:/students/" + studentId + "/courses";
    }

    @PostMapping("/students/{id}/upload-avatar")
    public String uploadStudentAvatar(@PathVariable("id") Long id,
            @RequestParam("avatar") MultipartFile file,
            RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file");
            return "redirect:/students";
        }

        try {
            StringBuilder fileNames = new StringBuilder();
            Student student = stuService.getStudent(id);
            String uploadDir = "src\\main\\resources\\static\\Assets\\img";
            Path fileNameAndPath = Paths.get(uploadDir, file.getOriginalFilename());
            fileNames.append(file.getOriginalFilename());
            Files.write(fileNameAndPath, file.getBytes());
            student.setAvatarImg(file.getOriginalFilename());
            stuService.save(student);
            ra.addFlashAttribute("message", "Avatar updated successfully for " + student.getStudentName());
        } catch (IOException | StudentNotFoundException e) {
            ra.addFlashAttribute("error", "Could not upload avatar: " + e.getMessage());
        }

        return "redirect:/students";
    }
}