package com.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;


import com.class_management.entity.Teacher;
import com.class_management.service.TeacherService;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name,
                              @RequestParam String phoneNumber,
                              @RequestParam(required = false) String newPassword,
                              @RequestParam(required = false) String confirmPassword,
                              Authentication authentication,
                              RedirectAttributes ra) {
        Teacher teacher = (Teacher) authentication.getPrincipal();
        
        try {
            // Update basic info
            teacher.setName(name);
            teacher.setPhoneNumber(phoneNumber);
            
            // Update password if provided
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    ra.addFlashAttribute("error", "Passwords don't match");
                    return "redirect:/home";
                }
                teacher.setPassword(passwordEncoder.encode(newPassword));
            }
            
            teacherService.updateTeacher(teacher);
            ra.addFlashAttribute("message", "Profile updated successfully");
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        
        return "redirect:/home";
    }
}
