package com.class_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.class_management.entity.Teacher;
import com.class_management.repository.TeacherRepository;

@Service
public class TeacherDetailService implements UserDetailsService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        Teacher teacher = teacherRepository
                .findByEmailOrPhoneNumber(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("No teacher found with email or phone: " + input));

        System.out.println("Teacher found: " + teacher.getEmail() + ", Enabled: " + teacher.isEnabled());
        return teacher;
    }

}
