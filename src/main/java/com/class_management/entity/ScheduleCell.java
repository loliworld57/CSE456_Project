package com.class_management.entity;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleCell {
    private String courseId;
    private Course course;
    private LocalTime startTime;
    private double periods;
    private int rowspan;
    public boolean visible;

    public ScheduleCell(String courseId, Course course, LocalTime startTime, double periods, int rowspan,
            boolean visible) {
        this.courseId = courseId;
        this.course = course;
        this.startTime = startTime;
        this.periods = periods;
        this.rowspan = rowspan;
        this.visible = visible;
    }

    // Getters
    public String getCourseId() {
        return courseId;
    }

    public int getRowspan() {
        return rowspan;
    }

    public boolean isVisible() {
        return visible;
    }
}
