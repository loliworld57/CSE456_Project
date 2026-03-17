package com.class_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    @PositiveOrZero
    private long amount;

    @Column(length = 255)
    private String description;

    // Getters and Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Course getCourse() { 
        return course; 
    }

    public void setCourse(Course course) { 
        this.course = course; 
    }

    public long getAmount() { 
        return amount; 
    }

    public void setAmount(long amount) { 
        if (amount < 0) {
            throw new IllegalArgumentException("Expense cannot be negative");
        }
        this.amount = amount; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(String description) { 
        this.description = description; 
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}