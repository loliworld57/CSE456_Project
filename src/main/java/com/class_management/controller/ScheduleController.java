package com.class_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.class_management.entity.Course;
import com.class_management.entity.Schedule;
import com.class_management.entity.ScheduleCell;
import com.class_management.entity.Teacher;
import com.class_management.exception.CourseNotFoundException;
import com.class_management.service.CourseService;
import com.class_management.service.ScheduleService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ScheduleController {

    private static final String PAGE_NAME = "Schedule";
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/schedule")
    public String showSchedule(@AuthenticationPrincipal Teacher teacher, Model model) {
        List<Schedule> schedules = scheduleService.findByTeacher(teacher);
        List<Course> courses = courseService.getCoursesByTeacher(teacher);

        for (Schedule s : schedules) {
            if (s.getEndTime() == null) {
                s.calculateEndTime();
            }

            System.out.println("Schedule: " + s.getCourse().getCourseId() + " on " + s.getDayOfWeek() + " at "
                    + s.getStartTime() + " to " + s.getEndTime());
        }

        List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        List<LocalTime> timeSlots = new ArrayList<>();
        for (int hour = 7; hour <= 21; hour++) {
            timeSlots.add(LocalTime.of(hour, 30));
        }

        Map<String, Map<LocalTime, ScheduleCell>> scheduleCellTable = new LinkedHashMap<>();

        for (String day : days) {
            Map<LocalTime, ScheduleCell> cellMap = new LinkedHashMap<>();
            for (LocalTime time : timeSlots) {
                cellMap.put(time, new ScheduleCell("", null, time, 0, 1, false));
            }
            scheduleCellTable.put(day, cellMap);
        }
        boolean[][] occupiedSlots = new boolean[days.size()][timeSlots.size()];
        for (Schedule schedule : schedules) {
            String day = schedule.getDayOfWeek().name();
            LocalTime start = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();
            Course course = schedule.getCourse();
            String courseId = course.getCourseId();
            double periods = schedule.getPeriods();

            int startIndex = timeSlots.indexOf(start);
            int endIndex = timeSlots.indexOf(end);
            int rowspan = endIndex - startIndex;
            
            for (int i = startIndex; i < endIndex; i++) {
                occupiedSlots[days.indexOf(day)][i] = true;
            }

            // Mark the first slot with full course info
            scheduleCellTable.get(day).put(start,
                    new ScheduleCell(courseId, course, start, periods, rowspan, true));

        }
        
        System.out.println(schedules);

        model.addAttribute("teacher", teacher);
        model.addAttribute("pageName", PAGE_NAME);
        model.addAttribute("schedules", schedules);
        model.addAttribute("scheduleTable", scheduleCellTable);
        model.addAttribute("days", days);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("courses", courses);
        model.addAttribute("occupiedSlots", occupiedSlots);
        return "schedule";
    }

    @GetMapping("/manageSchedule")
    public String manageSchedule(@AuthenticationPrincipal Teacher teacher, Model model) {
        List<Schedule> schedules = scheduleService.findByTeacher(teacher);
        List<Course> courses = courseService.getCoursesByTeacher(teacher);

        model.addAttribute("teacher", teacher);
        model.addAttribute("pageName", PAGE_NAME);
        model.addAttribute("schedules", schedules);
        model.addAttribute("courses", courses);
        return "manage_schedule";
    }

    @PostMapping("/schedule/add")
    public String addSchedule(@AuthenticationPrincipal Teacher teacher,
            @RequestParam String courseId,
            @RequestParam String dayOfWeek,
            @RequestParam String startTime,
            @RequestParam double periods,
            RedirectAttributes ra) {
        try {
            Course course;
            try {
                course = courseService.getCourse(courseId);
            } catch (CourseNotFoundException ex) {
                ra.addFlashAttribute("error", "Course not found");
                return "redirect:/manageSchedule";
            }
            if (course == null) {
                ra.addFlashAttribute("error", "Course not found");
                return "redirect:/manageSchedule";
            }

            LocalTime start = LocalTime.parse(startTime);
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek);

            LocalTime end = start.plusMinutes((long) (periods * 60));
            if (!scheduleService.isTimeSlotAvailable(teacher, day, start, end)) {
                ra.addFlashAttribute("error", "Time slot is not available");
                return "redirect:/manageSchedule";
            }

            Schedule schedule = new Schedule();
            schedule.setCourse(course);
            schedule.setTeacher(teacher);
            schedule.setDayOfWeek(day);
            schedule.setStartTime(start);
            schedule.setPeriods(periods);
            schedule.calculateEndTime();

            scheduleService.save(schedule);
            ra.addFlashAttribute("message", "Schedule added successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error adding schedule: " + e.getMessage());
        }
        return "redirect:/manageSchedule";
    }

    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes ra) {
        try {
            scheduleService.delete(scheduleService.findById(id));
            ra.addFlashAttribute("message", "Schedule deleted successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting schedule: " + e.getMessage());
        }
        return "redirect:/manageSchedule";
    }

    @GetMapping("/schedule/get/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.findById(id);
            if (schedule != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", schedule.getId());
                response.put("courseId", schedule.getCourse().getCourseId());
                response.put("dayOfWeek", schedule.getDayOfWeek().name());
                // Format time with leading zeros
                response.put("startTime", String.format("%02d:30", schedule.getStartTime().getHour()));
                response.put("periods", schedule.getPeriods());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/schedule/update/{id}")
    public String updateSchedule(@AuthenticationPrincipal Teacher teacher,
            @RequestParam Long id,
            @RequestParam String courseId,
            @RequestParam String dayOfWeek,
            @RequestParam String startTime,
            @RequestParam double periods,
            RedirectAttributes ra) {
        try {
            Schedule schedule = scheduleService.findById(id);
            if (schedule == null) {
                ra.addFlashAttribute("error", "Schedule not found");
                return "redirect:/manageSchedule";
            }

            Course course;
            try {
                course = courseService.getCourse(courseId);
            } catch (CourseNotFoundException ex) {
                ra.addFlashAttribute("error", "Course not found");
                return "redirect:/manageSchedule";
            }
            if (course == null) {
                ra.addFlashAttribute("error", "Course not found");
                return "redirect:/manageSchedule";
            }

            LocalTime start = LocalTime.parse(startTime);
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek);
            LocalTime end = start.plusMinutes((long) (periods * 60));

            // Check availability excluding current schedule
            if (!scheduleService.isTimeSlotAvailable(teacher, day, start, end)) {
                ra.addFlashAttribute("error", "Time slot is not available");
                return "redirect:/manageSchedule";
            }

            schedule.setCourse(course);
            schedule.setDayOfWeek(day);
            schedule.setStartTime(start);
            schedule.setPeriods(periods);
            schedule.calculateEndTime();

            scheduleService.save(schedule);
            ra.addFlashAttribute("message", "Schedule updated successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating schedule: " + e.getMessage());
        }
        return "redirect:/manageSchedule";
    }

}