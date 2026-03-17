package com.class_management.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.class_management.entity.Teacher;

@Controller
public class LayoutController {

    @GetMapping("/layout")
    public String showMainLayout(Model model, Authentication authentication,
            @AuthenticationPrincipal Teacher currentTeacher) {
        Teacher teacher = (Teacher) authentication.getPrincipal();
        model.addAttribute("teacher", teacher);
        return "main_layout";
    }

}