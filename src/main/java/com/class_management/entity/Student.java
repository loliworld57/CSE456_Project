package com.class_management.entity;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", columnDefinition = "BIGINT")
    private Long studentId;

    @Column(nullable = false, length = 50)
    private String studentName;

    @Column(nullable = false, length = 50)
    private String entryDate;

    private char performance;

    @Column(nullable = false, length = 12)
    private String parentNumber;

    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments = new HashSet<>();

    public Set<Enrollment> getEnrollments() {
        return enrollments;
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

    @Column(length = 255)
    private String avatarImg;

    public String getAvatarImg() {
        if (avatarImg == null || avatarImg.isEmpty()) {
            return "/Assets/img/default-avatar.png";
        }
        return "/Assets/img/" + avatarImg;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public char getPerformance() {
        return performance;
    }

    public void setPerformance(char performance) {
        this.performance = performance;
    }

    public String getParentNumber() {
        return parentNumber;
    }

    public void setParentNumber(String parentNumber) {
        this.parentNumber = parentNumber;
    }

    public void calculateAndUpdatePerformance() {
        if (enrollments.isEmpty()) {
            this.performance = 'N';
            return;
        }

        float totalScore = 0;
        for (Enrollment enrollment : enrollments) {
            totalScore += enrollment.getFinalScore();
        }
        float averageScore = totalScore / enrollments.size();

        float[] scoreRanges = { 0, 3.5f, 5, 6.5f, 8 };
        char[] performanceRanges = { 'F', 'D', 'C', 'B', 'A' };

        for (int i = 0; i < scoreRanges.length; i++) {
            if (averageScore > scoreRanges[i]) {
                this.performance = performanceRanges[i];
                return;
            }
        }
    }

    public String getPerformanceDescription() {
        return switch (performance) {
            case 'A' -> "Excellent (≥8.0)";
            case 'B' -> "Good (6.5-7.9)";
            case 'C' -> "Average (5.0-6.4)";
            case 'D' -> "Try Harder (3.5-4.9)";
            case 'F' -> "Fail (<3.5)";
            default -> "Not Graded";
        };
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", entryDate='" + entryDate + '\'' +
                ", performance=" + performance +
                ", parentNumber='" + parentNumber + '\'' +
                '}';
    }

}
