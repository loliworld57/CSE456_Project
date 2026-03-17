package com.class_management.repository;

import com.class_management.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByPhoneNumber(String phoneNumber);
    Teacher findByTeacherId(Long teacherId);
    Optional<Teacher> findByEmailOrPhoneNumber(String email, String phoneNumber);
}