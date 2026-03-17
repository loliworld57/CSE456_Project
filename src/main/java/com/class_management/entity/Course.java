package com.class_management.entity;

import java.util.*;
import jakarta.persistence.*;

@Entity
@Table(name = "courses")

public class Course {
    @Id
    @Column(nullable = false, length = 10)
    private String courseId;

    @Column(nullable = false, length = 50)
    private String courseSubject;

    @Column(length = 50)
    private String courseName;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Expense> expenses = new HashSet<>();

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Revenue> revenues = new HashSet<>();

    public Set<Revenue> getRevenues() {
        return revenues;
    }

    public void setRevenues(Set<Revenue> revenues) {
        this.revenues = revenues;
    }

    public long getTotalRevenues() {
        return revenues.stream()
                .mapToLong(Revenue::getAmount)
                .sum();
    }

    public long getNetIncome() {
        return getTotalRevenues() - getTotalExpenses();
    }

    @OneToMany(mappedBy = "course")
    private Set<Enrollment> enrollments = new HashSet<>();

    public Set<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    @ManyToOne
    @JoinColumn(name = "teacher_id", columnDefinition = "BIGINT")
    private Teacher teacher;

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Course() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String number) {
        if (this.courseSubject != null) {
            String prefix = this.courseSubject.length() >= 3 ? this.courseSubject.substring(0, 3).toUpperCase()
                    : this.courseSubject.toUpperCase();
            this.courseId = prefix + "_" + number;
        }
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public long getTotalExpenses() {
        return expenses.stream()
                .mapToLong(Expense::getAmount)
                .sum();
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseSubject='" + courseSubject + '\'' +
                ", courseName='" + courseName + '\'' +
                '}';
    }

}