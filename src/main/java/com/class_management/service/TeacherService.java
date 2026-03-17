package com.class_management.service;

import com.class_management.entity.Role;
import com.class_management.entity.Teacher;
import com.class_management.repository.TeacherRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Teacher registerNewTeacher(String name, String email, String phoneNumber, String password) {
        if (teacherRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setPhoneNumber(phoneNumber);
        teacher.setPassword(passwordEncoder.encode(password));
        teacher.setRole(Role.USER);
        teacher.setEnabled(true);

        return teacherRepository.save(teacher);
    }

    public Teacher findByEmail(String email) {
        return teacherRepository.findByEmail(email).orElse(null);
    }

    public Teacher findByPhone(String phoneNumber) {
        return teacherRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public Teacher updateTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public void save(Teacher teacher) {
        teacherRepository.save(teacher);
    }

    public List<Teacher> getAllTeachers() {
        return (List<Teacher>) teacherRepository.findAll();
    }

    public void updateTeacherRole(Long teacherId, Role newRole) {
        Teacher teacher = teacherRepository.findByTeacherId(teacherId);
        if (teacher != null) {
            teacher.setRole(newRole);
            teacherRepository.save(teacher);
        } else {
            throw new RuntimeException("Teacher not found");
        }
    }

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findByTeacherId(id);
    }

    public void setDisable(Long id) {
        Teacher teacher = teacherRepository.findByTeacherId(id);
        if (teacher != null) {
            teacher.setEnabled(false);
            teacherRepository.save(teacher);
        } else {
            throw new RuntimeException("Teacher not found");
        }
    }

    public void setEnable(Long id) {
        Teacher teacher = teacherRepository.findByTeacherId(id);
        if (teacher != null) {
            teacher.setEnabled(true);
            teacherRepository.save(teacher);
        } else {
            throw new RuntimeException("Teacher not found");
        }
    }

}