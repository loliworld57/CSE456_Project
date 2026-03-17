package com.class_management.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.class_management.entity.Schedule;
import com.class_management.entity.Teacher;
import com.class_management.entity.Course;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends CrudRepository<Schedule, Long> {

    List<Schedule> findByCourse(Course course);

    boolean existsByTeacherTeacherIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
            Long teacherId,
            DayOfWeek dayOfWeek,
            LocalTime endTime,
            LocalTime startTime);

    List<Schedule> findByTeacher(Teacher teacher);
    
    

}