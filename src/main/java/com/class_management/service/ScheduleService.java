package com.class_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.class_management.entity.Schedule;
import com.class_management.repository.ScheduleRepository;
import com.class_management.entity.Course;
import com.class_management.entity.Teacher;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    public boolean isTimeSlotAvailable(Teacher teacher, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        boolean hasOverlap = scheduleRepository
                .existsByTeacherTeacherIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
                        teacher.getTeacherId(),
                        dayOfWeek,
                        endTime,
                        startTime);

        return !hasOverlap;
    }

    public List<Schedule> findByTeacher(Teacher teacher) {
        return scheduleRepository.findByTeacher(teacher);
    }

    public List<Schedule> findByCourse(Course course) {
        return scheduleRepository.findByCourse(course);
    }

    public void save(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    public Schedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
    }

    public void delete(Schedule schedule) {
        if (scheduleRepository.findById(schedule.getId()).isEmpty()) {
            throw new IllegalArgumentException("Schedule not found for deletion");
        }
        scheduleRepository.delete(schedule);
    }

    public void deleteAll(List<Schedule> schedules) {
        scheduleRepository.deleteAll(schedules);
    }

}