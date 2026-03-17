package com.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.class_management.service.TeacherService;

@Controller
public class SignUpController {
    @Autowired
    private TeacherService teacherService;

    @GetMapping("/signup")
    public String showSignUpPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerTeacher(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes rA) {

     
        if (name.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
            rA.addFlashAttribute("error", "All fields are required.");
            return "redirect:/signup";
        }

  
        if (name.length() <= 5) {
            rA.addFlashAttribute("error", "Name must be longer than 5 characters.");
            return "redirect:/signup";
        }

       
        if (!phoneNumber.matches("\\d{10}")) {
            rA.addFlashAttribute("error", "Phone number must be exactly 10 digits.");
            return "redirect:/signup";
        }

 
        if (password.length() <= 6) {
            rA.addFlashAttribute("error", "Password must be longer than 6 characters.");
            return "redirect:/signup";
        }

      
        if (!password.equals(confirmPassword)) {
            rA.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/signup";
        }

       
        if (teacherService.findByEmail(email) != null) {
            rA.addFlashAttribute("error", "Email is already in use.");
            return "redirect:/signup";
        }

    
        if (teacherService.findByPhone(phoneNumber) != null) {
            rA.addFlashAttribute("error", "Phone number is already in use.");
            return "redirect:/signup";
        }

  
        try {
            teacherService.registerNewTeacher(name, email, phoneNumber, password);
            return "redirect:/login";
        } catch (Exception e) {
            rA.addFlashAttribute("error", "Registration failed. Please try again.");
            return "redirect:/signup";
        }
    }

}
