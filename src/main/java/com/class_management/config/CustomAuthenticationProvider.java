package com.class_management.config;

import java.util.Collections;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.class_management.entity.Teacher;
import com.class_management.repository.TeacherRepository;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)  {
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();
        // Check if the login is an email or phone number
        Optional<Teacher> teacher = teacherRepository.findByEmail(login);
        if (teacher.isEmpty()) {
            teacher = teacherRepository.findByPhoneNumber(login);
        }

        if (teacher.isPresent() && passwordEncoder.matches(password, teacher.get().getPassword())) {
            // If the login is valid, return an authentication token
            return new UsernamePasswordAuthenticationToken(
                    teacher.get(),
                    password,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + teacher.get().getRole().name())));
        }

        throw new BadCredentialsException("Invalid username/password");

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
