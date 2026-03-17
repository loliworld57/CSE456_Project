package com.class_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Date;

@Entity
@Table(name = "enrollments")

public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_date", nullable = false)
    private Date enrollmentDate;

    @Column(nullable = true)
    @Min(value = 0)
    @Max(value = 10)
    private Float progressScore;

    @Column(nullable = true)
    @Min(value = 0)
    @Max(value = 10)
    private Float testScore;

    @Column(nullable = true)
    private Float finalScore;

    @Column(nullable = true)
    private Character performance;

    public Character getPerformance() {
        return performance;
    }

    public void setPerformance(Character performance) {
        this.performance = performance;
    }

    public Enrollment() {
        this.enrollmentDate = new Date();
    }

    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.enrollmentDate = new Date();
    }

    public Float getProgressScore() {
        return progressScore;
    }

    public void updatePerformance() {
        Float finalScore = this.finalScore;
        if (finalScore == null) {
            this.performance = 'F';
            return;
        }

        float[] scoreRanges = { 0, 3.5f, 5, 6.5f, 8 };
        char[] performanceRanges = { 'F', 'D', 'C', 'B', 'A' };

        for (int i = 0; i < scoreRanges.length; i++) {
            if (finalScore >= scoreRanges[i]) {
                this.performance = performanceRanges[i];
            }
        }
    }

    public void setProgressScore(Float progressScore) {
        if (progressScore != null && (progressScore < 0 || progressScore > 10)) {
            throw new IllegalArgumentException("Progress score must be between 0 and 10");
        }
        this.progressScore = progressScore;
        this.setFinalScore();
        this.updatePerformance();
    }

    public Float getTestScore() {
        return this.testScore;
    }

    public void setTestScore(Float testScore) {
        if (testScore != null && (testScore < 0 || testScore > 10)) {
            throw new IllegalArgumentException("Test score must be between 0 and 10");
        }
        this.testScore = testScore;
        this.setFinalScore();
        this.updatePerformance();
    }

    public void setFinalScore() {
        if (this.progressScore == null || this.testScore == null) {
            this.finalScore = null;
            return;
        }
        this.finalScore = (this.progressScore + (this.testScore * 2)) / 3;
    }

    public Float getFinalScore() {
        return this.finalScore;
    }

    public long getId() {
        return enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", student=" + student +
                ", course=" + course +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }

}
