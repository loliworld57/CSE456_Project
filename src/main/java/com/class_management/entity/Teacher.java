package com.class_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@Table(name = "teachers")
public class Teacher implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id", columnDefinition = "BIGINT")
    private Long teacherId;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(length = 15, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // Default role is USER

    @OneToMany(mappedBy = "teacher")
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "teacher")
    private Set<Course> courses = new HashSet<>();

    @Column(length = 255)
    private String avatarImg;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;


    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public String getAvatarImgPath() {
        if (avatarImg == null)
            return "/Assets/images/kimco.jpg";
        return "/Assets/img/" + avatarImg;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void addCourse(Course course) {
        courses.add(course);
        course.setTeacher(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setTeacher(null);
    }

}
