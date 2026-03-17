package com.class_management.exception;

public class CourseNotFoundException extends Throwable {
    public CourseNotFoundException(String message) {
        super(message);
    }
}
