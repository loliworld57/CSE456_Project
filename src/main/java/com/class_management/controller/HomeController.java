package com.class_management.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.class_management.entity.Course;
import com.class_management.entity.Enrollment;
import com.class_management.entity.Role;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.service.CourseService;
import com.class_management.service.EnrollmentService;
import com.class_management.service.StudentService;
import com.class_management.service.TeacherService;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService stuService;

    // Redirect root path to /home
    @GetMapping("")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String showHomePage(Model model, Authentication authentication,
            @AuthenticationPrincipal Teacher currentTeacher) {

        if (currentTeacher.getRole() == Role.ADMIN) {
        return "redirect:/admin";
    }
    
        List<Course> courses = courseService.getCoursesByTeacher(currentTeacher);
        List<Student> listStudents = stuService.getStudentsByTeacher(currentTeacher);
        List<Float> allScores = enrollmentService.getAllScoresByTeacher(currentTeacher);

        List<Enrollment> topStudents = enrollmentService.findTop10ByTeacherIdOrderByFinalScoreDesc(currentTeacher);

        long totalRevenues = courses.stream().mapToLong(Course::getTotalRevenues).sum();
        long totalExpenses = courses.stream().mapToLong(Course::getTotalExpenses).sum();
        long totalNetIncome = totalRevenues - totalExpenses;

        // pie chart
        Map<String, Long> classStudentCounts = new LinkedHashMap<>();
        for (Course course : courses) {
            long count = course.getEnrollments().size();
            classStudentCounts.put(course.getCourseName(), count);
        }
        // polar chart
        Map<String, Long> scoreBuckets = new LinkedHashMap<>();
        scoreBuckets.put("null", 0L);
        scoreBuckets.put("<5", 0L);
        scoreBuckets.put("<7", 0L);
        scoreBuckets.put("<9", 0L);
        scoreBuckets.put(">=9", 0L);

        for (Float score : allScores) {

            if (score == null) {
                scoreBuckets.put("null", scoreBuckets.get("null") + 1);
            } else if (score < 5) {
                scoreBuckets.put("<5", scoreBuckets.get("<5") + 1);
            } else if (score < 7) {
                scoreBuckets.put("<7", scoreBuckets.get("<7") + 1);
            } else if (score < 9) {
                scoreBuckets.put("<9", scoreBuckets.get("<9") + 1);
            } else {
                scoreBuckets.put(">=9", scoreBuckets.get(">=9") + 1);
            }
        }

        // group bar chart
        Map<String, Long> revenuesMap = new LinkedHashMap<>();
        Map<String, Long> expensesMap = new LinkedHashMap<>();
        Map<String, Long> netMap = new LinkedHashMap<>();

        for (Course course : courses) {
            String courseId = course.getCourseId();
            long revenue = course.getTotalRevenues();
            long expenses = course.getTotalExpenses();
            long net = revenue - expenses;

            if (net == 0 && revenue == 0 && expenses == 0) {
                continue;
            }
            revenuesMap.put(courseId, revenue);
            expensesMap.put(courseId, -expenses);
            netMap.put(courseId, net);

        }

        model.addAttribute("courseId", revenuesMap.keySet());
        model.addAttribute("revenues", revenuesMap.values());
        model.addAttribute("expenses", expensesMap.values());
        model.addAttribute("netIncomes", netMap.values());

        model.addAttribute("scoreLabels", scoreBuckets.keySet());
        model.addAttribute("scoreCounts", scoreBuckets.values());

        model.addAttribute("classLabels", classStudentCounts.keySet());
        model.addAttribute("classCounts", classStudentCounts.values());

        model.addAttribute("topStudents", topStudents);
        model.addAttribute("listStudents", listStudents);
        model.addAttribute("teacher", currentTeacher);
        model.addAttribute("listCourses", courses);
        model.addAttribute("totalRevenues", totalRevenues);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalNetIncome", totalNetIncome);

        
        return "index";
    }

    @PostMapping("/upload-avatar")
    public String uploadTeacherAvatar(Model model, @RequestParam("avatar") MultipartFile file,
            RedirectAttributes ra, Authentication authentication,
            @AuthenticationPrincipal Teacher currentTeacher) {

        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file");
            return "redirect:/home";
        }

        try {
            StringBuilder fileNames = new StringBuilder();
            String uploadDir = "src\\main\\resources\\static\\Assets\\img";
            Path fileNameAndPath = Paths.get(uploadDir, file.getOriginalFilename());
            fileNames.append(file.getOriginalFilename());
            Files.write(fileNameAndPath, file.getBytes());
            ra.addFlashAttribute("message", "Avatar updated!");
            currentTeacher.setAvatarImg(file.getOriginalFilename());
            teacherService.save(currentTeacher);

        } catch (IOException e) {
            ra.addFlashAttribute("message", "Upload failed: " + e.getMessage());
        }
        return "redirect:/home";
    }

}