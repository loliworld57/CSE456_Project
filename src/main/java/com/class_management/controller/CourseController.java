package com.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.class_management.entity.Course;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.exception.StudentNotFoundException;
import com.class_management.repository.ExpenseRepository;
import com.class_management.repository.RevenueRepository;
import com.class_management.service.CourseService;
import com.class_management.service.EnrollmentService;
import com.class_management.service.ScheduleService;
import com.class_management.service.StudentService;

import java.util.stream.Collectors;
import com.class_management.entity.Student;
import com.class_management.entity.Teacher;
import com.class_management.entity.Enrollment;
import com.class_management.entity.Expense;
import com.class_management.entity.Revenue;
import com.class_management.entity.Schedule;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.*;

@Controller
public class CourseController {
    @Autowired
    private CourseService couService;
    @Autowired
    private StudentService stuService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private RevenueRepository revenueRepository;
    @Autowired
    private ScheduleService scheService;

    private static final String PAGE_NAME = "Course Page";

    @GetMapping("/courses")
    public String showCourses(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal Teacher teacher,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        final int PAGE_SIZE = 10;
        Page<Course> coursePage = couService.searchCourses(keyword, teacher, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("teacher", teacher);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageName", PAGE_NAME);
        return "courses";
    }

    @GetMapping("/courselist")
    public String showCourseList(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal Teacher teacher,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        final int PAGE_SIZE = 8;
        Page<Course> coursePage = couService.searchCourses(keyword, teacher, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("teacher", teacher);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageName", PAGE_NAME);
        return "courselist";
    }

    @GetMapping("/courses/new")
    public String showNewCourse(@AuthenticationPrincipal Teacher currentTeacher, Model model) {
        Course course = new Course();
        course.setTeacher(currentTeacher);
        model.addAttribute("course", course);
        model.addAttribute("teacher", currentTeacher);
        model.addAttribute("pageTitle", "Add Course");
        model.addAttribute("pageName", PAGE_NAME);
        return "course_form";
    }

    @GetMapping("/courses/update/{id}")
    public String showEditCourse(@PathVariable("id") String id, Model model, RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            Course cou = couService.getCourse(id);
            model.addAttribute("course", cou);
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("pageTitle", "Edit Course " + id);
            model.addAttribute("pageName", PAGE_NAME);
            return "course_form";
        } catch (CourseNotFoundException e) {
            rA.addFlashAttribute("error", "Failed to Edit Course");
            return "redirect:/courses";
        }
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable("id") String id, RedirectAttributes rA) {
        try {
            Course course = couService.getCourse(id);

            // Check for enrolled students
            if (!course.getEnrollments().isEmpty()) {
                rA.addFlashAttribute("error",
                        "Cannot delete course " + id + ". Please remove all enrolled students first.");
                return "redirect:/courses";
            }

            // Delete associated schedules first
            List<Schedule> schedules = scheService.findByCourse(course);
            if (!schedules.isEmpty()) {
                scheService.deleteAll(schedules);
            }

            // Then delete the course
            couService.deleteCourse(id);
            rA.addFlashAttribute("message", "Course " + id + " and its schedules have been deleted");
        } catch (CourseNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/students")
    public String showCourseStudent(@PathVariable("id") String id,
            Model model,
            RedirectAttributes rA,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            final int PAGE_SIZE = 10;
            Course course = couService.getCourse(id);
            Page<Student> studentPage = enrollmentService.getStudentsByCourseId(id, PageRequest.of(page, PAGE_SIZE));
            model.addAttribute("course", course);
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("studentPage", studentPage);
            model.addAttribute("pageTitle", "Students enrolled in Course " + id);
            model.addAttribute("pageName", PAGE_NAME);

            return "enrolledstudents";
        } catch (CourseNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses";
        }
    }

    @GetMapping("/courses/{id}/enroll")
    public String enrollStudent(@PathVariable("id") String id, Model model, RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher)
            throws StudentNotFoundException, CourseNotFoundException {
        try {

            Course course = couService.getCourse(id);
            List<Student> availableStudents = stuService.getStudentsNotInCourse(id, currentTeacher);

            model.addAttribute("course", course);
            model.addAttribute("availableStudents", availableStudents);
            model.addAttribute("pageTitle", "Enroll Student in Course " + id);
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("pageName", PAGE_NAME);
            return "enroll_form";
        } catch (RuntimeException e) {
            rA.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses/{id}/students";
        }
    }

    @PostMapping("/courses/save")
    public String saveCourse(
            @AuthenticationPrincipal Teacher currentTeacher,
            Course course,
            @RequestParam("number") String number,
            RedirectAttributes rA) {
        try {
            course.setCourseSubject(course.getCourseSubject().toUpperCase());
            course.setCourseId(number);
            course.setTeacher(currentTeacher);
            couService.save(course);
            rA.addFlashAttribute("message", "The Course has been saved successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error saving course: " + e.getMessage());
        }
        return "redirect:/courses";
    }

    @PostMapping("/courses/{courseId}/enroll")
    public String processEnrollment(@PathVariable("courseId") String courseId,
            @RequestParam("studentId") Long studentId,
            RedirectAttributes rA) throws StudentNotFoundException, CourseNotFoundException {
        try {
            enrollmentService.enrollStudent(studentId, courseId);
            rA.addFlashAttribute("message", "Student has been successfully enrolled in the course");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error enrolling student: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/students";
    }

    @PostMapping("/courses/{courseId}/students/{studentId}/remove")
    public String removeStudentFromCourse(@PathVariable("courseId") String courseId,
            @PathVariable("studentId") Long studentId,
            RedirectAttributes rA) throws StudentNotFoundException, CourseNotFoundException {
        try {
            enrollmentService.unenrollStudent(studentId, courseId);
            rA.addFlashAttribute("message", "Student has been removed from the course");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error removing student: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/students";
    }

    @GetMapping("/courses/{id}/view-students")
    public String viewCourseStudents(@PathVariable("id") String id, Model model, RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            Course course = couService.getCourse(id);
            Set<Student> enrolledStudents = course.getEnrollments()
                    .stream()
                    .map(Enrollment::getStudent)
                    .collect(Collectors.toSet());

            model.addAttribute("course", course);
            model.addAttribute("enrolledStudents", enrolledStudents);
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("pageName", PAGE_NAME);
            return "course_students";
        } catch (CourseNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
            return "redirect:/courselist";
        }
    }

    @GetMapping("/courses/{id}/financial")
    public String showCourseExpenses(@PathVariable("id") String id, Model model, RedirectAttributes rA,
            @AuthenticationPrincipal Teacher currentTeacher) {
        try {
            Course course = couService.getCourse(id);
            List<Expense> expenses = expenseRepository.findByCourse(course);
            List<Revenue> revenues = revenueRepository.findByCourse(course);

            model.addAttribute("course", course);

            model.addAttribute("expenses", expenses);
            model.addAttribute("newExpense", new Expense());
            model.addAttribute("totalExpenses", course.getTotalExpenses());

            model.addAttribute("revenues", revenues);
            model.addAttribute("newRevenue", new Revenue());
            model.addAttribute("totalRevenues", course.getTotalRevenues());
            model.addAttribute("teacher", currentTeacher);
            model.addAttribute("netIncome", course.getNetIncome());
            model.addAttribute("pageName", PAGE_NAME);
            return "course_financial";
        } catch (CourseNotFoundException e) {
            rA.addFlashAttribute("error", e.getMessage());
            return "redirect:/courselist";
        }
    }

    @PostMapping("/courses/{courseId}/expenses/{expenseId}/delete")
    public String deleteExpense(@PathVariable("courseId") String courseId,
            @PathVariable("expenseId") Long expenseId,
            RedirectAttributes rA) {
        try {
            expenseRepository.deleteById(expenseId);
            rA.addFlashAttribute("message", "Expense deleted successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error deleting expense: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/financial";
    }

    @PostMapping("/courses/{courseId}/expenses/add")
    public String addExpense(@PathVariable("courseId") String courseId,
            @ModelAttribute Expense expense,
            RedirectAttributes rA) throws CourseNotFoundException {
        try {
            Course course = couService.getCourse(courseId);
            expense.setCourse(course);
            expenseRepository.save(expense);
            rA.addFlashAttribute("message", "Expense added successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error adding expense: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/financial";
    }

    @PostMapping("/courses/{courseId}/revenues/{revenueId}/delete")
    public String deleteRevenue(@PathVariable("courseId") String courseId,
            @PathVariable("revenueId") Long revenueId,
            RedirectAttributes rA) {
        try {
            revenueRepository.deleteById(revenueId);
            rA.addFlashAttribute("message", "Revenue deleted successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error deleting revenue: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/financial";
    }

    @PostMapping("/courses/{courseId}/revenues/add")
    public String addRevenue(@PathVariable("courseId") String courseId,
            @ModelAttribute Revenue revenue,
            RedirectAttributes rA) throws CourseNotFoundException {
        try {
            Course course = couService.getCourse(courseId);
            revenue.setCourse(course);
            revenueRepository.save(revenue);
            rA.addFlashAttribute("message", "Revenue added successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error adding revenue: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/financial";
    }

    @PostMapping("/courses/{courseId}/students/{studentId}/scores")
    public String updateScores(
            @PathVariable String courseId,
            @PathVariable Long studentId,
            @RequestParam Float progressScore,
            @RequestParam Float testScore,
            RedirectAttributes rA) throws StudentNotFoundException, CourseNotFoundException {
        try {
            Student student = stuService.getStudent(studentId);
            Enrollment enrollment = student.getEnrollments().stream()
                    .filter(e -> e.getCourse().getCourseId().equals(courseId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

            enrollment.setProgressScore(progressScore);
            enrollment.setTestScore(testScore);
            enrollment.updatePerformance();
            enrollmentService.save(enrollment);

            student.calculateAndUpdatePerformance();
            stuService.save(student);

            rA.addFlashAttribute("message", "Scores updated successfully");
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Error updating scores: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId + "/view-students";
    }

}
