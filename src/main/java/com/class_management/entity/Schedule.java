package com.class_management.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private double periods;

    @Column(nullable = false)
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    // Calculate end time based on start time and periods
    @PrePersist
    @PreUpdate
    public void calculateEndTime() {
        if (startTime == null || periods <= 0) {
            this.endTime = null;
            return;
        } else {
            long minutes = (long) (periods * 60);
            this.endTime = startTime.plusMinutes(minutes);
        }
        return;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", course=" + course +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", periods=" + periods +
                ", endTime=" + endTime +
                ", teacher=" + teacher +
                '}';
    }
    
}